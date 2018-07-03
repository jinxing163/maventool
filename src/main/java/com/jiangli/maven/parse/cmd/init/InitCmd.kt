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
@Component
class InitCmd: BaseCmd("init"){
    override fun process(config: Config) {
        val jsonConfig = Util.getFriendFile("conf.json", "conf.json不存在")

        val gson = Gson()
        val initJSONConfig = gson.fromJson<InitJSONConfig>(JsonReader(FileReader(jsonConfig)), InitJSONConfig::class.java)
        println(initJSONConfig)

        val env = getEnv()
        println(env)

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
                val targetJar = File(eachDir, currentJar.name)
                println("try copy jar:$targetJar")
                if (targetJar.name.endsWith(".jar")) {
                    targetJar.createNewFile()
                    IOUtils.copy(FileInputStream(currentJar),FileOutputStream(targetJar))
                }

                //all.bat
                Util.writeToFile("""
call del.bat
call udpate_version.bat
call package.bat
call deploy.bat
call recompile.bat
                """.trimIndent(), eachDir,"all.bat")
            }
        }

    }
}