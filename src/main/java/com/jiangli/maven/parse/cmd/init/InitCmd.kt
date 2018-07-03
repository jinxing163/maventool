package com.jiangli.maven.parse.cmd.init

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.jiangli.maven.parse.Config
import com.jiangli.maven.parse.Util
import com.jiangli.maven.parse.Util.getEnv
import com.jiangli.maven.parse.cmd.BaseCmd
import org.apache.commons.io.IOUtils
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

    return initJSONConfig
}

@Component
class InitCmd: BaseCmd("init"){
    override fun process(config: Config) {
        val initJSONConfig = getJsonConfig()
        println("【INIT-CONFIG】:$initJSONConfig")

        val env = getEnv()
        println("env:$env")

        var dir = Util.getFriendFile(config.initDirName.toString())
        println("getDir:$dir")

        if (dir == null) {
            dir = Util.createFriendDir(config.initDirName.toString())
            println("createDir:$dir")
        } else {
            Util.deleteUnderDir(dir.absolutePath)
            println("deleteFilesUnderDir:$dir")
        }


        initJSONConfig.configs.forEach {
            val eachDir = File(dir,it.profile_id)
            if (!eachDir.exists()) {
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
                    IOUtils.copy(FileInputStream(currentJar),FileOutputStream(targetJar))
                }

                //all.bat
                Util.writeToFile("""
call del.bat
call update_version.bat
call package.bat
call deploy.bat
call recompile.bat
                """.trimIndent(), eachDir,"all.bat")

                //del.bat
                Util.writeToFile("""
del "${it.jarName}"
echo  deleted
                """.trimIndent(), eachDir,"del.bat")

                //update_version.bat
                Util.writeToFile("""
java -version
java -jar ${jarName}
                """.trimIndent(), eachDir,"update_version.bat")

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
                """.trimIndent(), eachDir,"package.bat")

                //deploy.bat
                Util.writeToFile("""
for /f "delims=" %%a in ('dir /b/a-d/oN *.version') do (
set jarVersion=%%a
)
set jarVersion=%jarVersion:~0,-8%

call mvn deploy:deploy-file -DgroupId=${it.groupId} -DartifactId=${it.artifaceId} -Dversion=%jarversion% -Dpackaging=jar -Dfile=${it.jarName} -Durl=${initJSONConfig.deploy_url} -DrepositoryId=${initJSONConfig.deploy_repositoryId}
                """.trimIndent(), eachDir,"deploy.bat")

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
                """.trimIndent(), eachDir,"recompile.bat")

            } //end of !exists
        }//end of each

    }

}