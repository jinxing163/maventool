package com.jiangli.maven.parse.cmd.init

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.jiangli.maven.parse.Config
import com.jiangli.maven.parse.Util
import com.jiangli.maven.parse.Util.getEnv
import com.jiangli.maven.parse.cmd.BaseCmd
import com.jiangli.maven.parse.cmd.version.createCurrentVersionFile
import com.jiangli.maven.parse.cmd.version.createDependencyFile
import com.jiangli.maven.parse.findRepoPath
import org.apache.commons.io.IOUtils
import org.springframework.beans.BeanUtils
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileReader

/**
 *
 *
 * @author Jiangli
 * @date 2018/7/3 16:25
 */
fun getJsonConfig(): InitJSONConfig {
    val jsonConfig = Util.getFriendFile("conf.json", "conf.json不存在")

    val gson = Gson()
    val initJSONConfig = gson.fromJson<InitJSONConfig>(JsonReader(FileReader(jsonConfig)), InitJSONConfig::class.java)

    Util.resolveProps(initJSONConfig)

    return initJSONConfig
}

@Component
class InitCmd : BaseCmd("init") {
    override fun process(config: Config) {
        val initJSONConfig = getJsonConfig()
        println("【INIT-CONFIG】:$initJSONConfig")

        val env = getEnv()
        println("env:$env")

        var dir = Util.getFriendFile(initJSONConfig.targetDirName.toString())
        println("getDir:$dir")

        val repoPath = findRepoPath()
        println("本地maven库地址:$repoPath")

        if (dir == null) {
            dir = Util.createFriendDir(initJSONConfig.targetDirName.toString())
            println("createDir:$dir")
        } else {
            Util.deleteUnderDir(dir.absolutePath)
            println("deleteFilesUnderDir:$dir")
        }


        initJSONConfig.configs.forEach {
            val eachDir = File(dir, it.name ?: it.profile_id)
            if (!eachDir.exists()) {
                //clone config
                var eachConfig = Config()
                BeanUtils.copyProperties(config, eachConfig)
                BeanUtils.copyProperties(it, eachConfig)
                println("[CONFIG]each: $eachConfig")

                //create dir
                eachDir.mkdir()
                println("loop: $eachDir")

                //copy jar
                val currentJar = Util.getCurrentJar()
                val jarName = currentJar.name
                println("jar name:$jarName")

                val targetJar = File(eachDir, jarName)
                println("try copy jar:$targetJar")
                if (targetJar.name.endsWith(".jar")) {
                    targetJar.createNewFile()
                    IOUtils.copy(FileInputStream(currentJar), FileOutputStream(targetJar))
                }

                //all.bat
                Util.writeToFile("""
call del.bat
call update_version.bat
call package.bat
call deploy.bat
call recompile.bat
                """.trimIndent(), eachDir, "all.bat")

                //del.bat
                Util.writeToFile("""
del "${it.jarName}"
echo  deleted
                """.trimIndent(), eachDir, "del.bat")

                //update_version.bat
                Util.writeToFile("""
java -version
java -jar ${jarName}
                """.trimIndent(), eachDir, "update_version.bat")

                val currentDiskSymbol = Util.getCurrentDiskSymbol()
                val fileDiskSymbol = Util.getFileDiskSymbol(initJSONConfig.project_path.toString())

                //package.bat
                Util.writeToFile("""
for /f "delims=" %%a in ('dir /b/a-d/oN *.version') do (
set jarVersion=%%a
)

set jarVersion=%jarVersion:~0,-8%
echo %jarVersion%

set "batPath=%cd%"
set "projectPath=${initJSONConfig.project_path}"
set "targetName=${it.jarName}"
set "targetPath=%projectPath%\\target\\%targetName%"

echo generated path is :%targetPath%
echo destination path is: %batPath%\%targetName%

del "%batPath%\%targetName%"
echo  deleted

cd %projectPath%
${fileDiskSymbol}:

call mvn -P${it.profile_id} ${it.mvn_cmd}

echo maven over...

copy "%targetPath%" "%batPath%\%targetName%" /Y

cd %batPath%
${currentDiskSymbol}:
                """.trimIndent(), eachDir, "package.bat")

                //deploy.bat
                Util.writeToFile("""
for /f "delims=" %%a in ('dir /b/a-d/oN *.version') do (
set jarVersion=%%a
)
set jarVersion=%jarVersion:~0,-8%

call mvn deploy:deploy-file -DgroupId=${it.groupId} -DartifactId=${it.artifactId} -Dversion=%jarversion% -Dpackaging=jar -Dfile=${it.jarName} -Durl=${initJSONConfig.deploy_url} -DrepositoryId=${initJSONConfig.deploy_repositoryId}
                """.trimIndent(), eachDir, "deploy.bat")

                //recompile.bat
                Util.writeToFile("""
set "batPath=%cd%"
set "projectPath=${initJSONConfig.project_path}"

cd %projectPath%
${fileDiskSymbol}:

call mvn  clean compile  -DskipTests
echo recompile over...

cd %batPath%
${currentDiskSymbol}:
                """.trimIndent(), eachDir, "recompile.bat")

                //create x.version
                val nextVersion = createCurrentVersionFile(eachDir, eachConfig)

                //依赖.txt
                createDependencyFile(eachDir, eachConfig, nextVersion)

                //config.properties
                Util.writeToFile("""
config.cmd=version

config.groupId=${it.groupId}
config.artifactId=${it.artifactId}
config.retreiveIdx=1
config.defaultVersion=1.0.0
config.nextAddOffset=0.0.1
config.weightOfEach=0.10.20
                """.trimIndent(), eachDir, "config.properties")

                //config.properties
                Util.writeToFile("""
前提：
1.maven配置中的的settings.xml需要配置如下server,用于上传jar时的认证
 <server>
	<id>thirdparty</id>
	<username>${initJSONConfig.maven_username}</username>
	<password>${initJSONConfig.maven_pwd}</password>
</server>

2.需要配置环境变量，例如
MVN_HOME
D:\apache-maven-3.2.5

Path
;%MVN_HOME%\bin    (附加到最后)

说明：
1.jar文件名必须为这种格式 openapi-treenity-xxxxxxx.jar
2.当前文件夹只能有一个这种格式的的jar，如果有多个，只会有一个(随机地)被上传

附录:
1.搜索路径
${Util.getMavenPath(eachConfig)}
2.maven 地址
${initJSONConfig.maven_search_url}
3.maven账号密码
${initJSONConfig.maven_username} ${initJSONConfig.maven_pwd}
                """.trimIndent(), eachDir, "使用说明.txt")


                Util.getMavenPath(eachConfig)

                repoPath?.let {
                    //openLocalDependencyDir.bat
                    Util.writeToFile("""
start ${repoPath}/${Util.getMavenPath(eachConfig)}
                """.trimIndent(), eachDir, "openLocalDependencyDir.bat")

                    //deleteLocalDependencyByVersion.bat
                    Util.writeToFile("""
for /f "delims=" %%a in ('dir /b/a-d/oN *.version') do (
set jarVersion=%%a
)
set jarVersion=%jarVersion:~0,-8%

rmdir  "${repoPath}/${Util.getMavenPath(eachConfig)}/%jarVersion%" /s /q
                """.trimIndent(), eachDir, "deleteLocalDependencyByVersion.bat")
                }

            } //end of !exists
        }//end of each


    }

}