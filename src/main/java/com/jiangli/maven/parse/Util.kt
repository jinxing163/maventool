package com.jiangli.maven.parse

import org.apache.commons.io.IOUtils
import org.springframework.boot.ApplicationHome
import org.springframework.util.PropertyPlaceholderHelper
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 *
 *
 * @author Jiangli
 * @date 2018/7/3 16:14
 */

object Util{
    fun getMavenPath(c:Config): String {
        var path="""${c.groupId?.replaceAll("\\.","/")}/${c.artifactId?.replaceAll("\\.", "/")}"""
        return path
    }

    /**
     * 获取当前jar包所在系统中的目录
     */
    fun getCurJarParentDir(): File {
        val jar = getCurrentJar()
        val pfile = jar.parentFile
        return pfile
    }

    fun getCurrentJar(): File {
        val home = ApplicationHome(javaClass)
        val jarFile = home.source ?: return home.dir
        return jarFile
    }
    fun getCurrentDiskSymbol(): String {
        val jar = getCurrentJar()
        return getFileDiskSymbol(jar.absolutePath)
    }
    fun getFileDiskSymbol(name:String): String {
        return name.substring(0,name.indexOf(":"))
    }

    fun getFriendFile(name:String): File? {
        val propFile = File(getCurJarParentDir(), name)
        if (!propFile.exists()) {
            return null
        }
        return propFile
    }
    fun createFriendFile(name:String): File? {
        val propFile = File(getCurJarParentDir(), name)
        if (!propFile.exists()) {
             propFile.createNewFile()
        }
        return propFile
    }
    fun createFriendDir(name:String): File? {
        val propFile = File(getCurJarParentDir(), name)
        if (!propFile.exists()) {
             propFile.mkdirs()
        }
        return propFile
    }
    fun getFriendFile(name:String,msg:String): File {
        val friendFile = getFriendFile(name) ?: throw IllegalAccessException(msg)
        return friendFile
    }
    fun resolveProps(obj:Any,properties:Properties) {
        val x =  PropertyPlaceholderHelper("\${","}",":",false)
        val fields = obj.javaClass.declaredFields
        for (field in fields) {
            field.isAccessible = true
            if (field.type == String::class.java) {
                val old = field.get(obj) as String?

                if (old != null) {
                    val newVal  = x.replacePlaceholders(old, properties)
                    field.set(obj,newVal)
                }
            }

            if (field.type == List::class.java) {
                val old = field.get(obj) as List<*>?

                old?.filterNotNull()
                  ?.forEach { resolveProps(it,properties) }
            }

        }
    }

    fun resolveProps(obj:Any) {
        val properties = Properties()
        val env = getEnv()
        properties.putAll(env)

        resolveProps(obj,properties)
    }

    fun getEnv(): MutableMap<String, String> {
        val ret:MutableMap<String,String> = mutableMapOf()
        val properties = System.getProperties()
        val env = System.getenv()

        ret.putAll(env)
        properties.forEach { t, u ->
            ret.put(t.toString(),u.toString())
        }

        ret.put("jarPath", getCurJarParentDir().absolutePath)
        return ret
    }

//    fun resolveValue(name:String,context:Map<String, String>):String  {
//        TODO()
//    }

    fun deleteFilesUnderDir(dirPath: String): Int {
        val dir = File(dirPath)
        var count = 0
        if (dir.isDirectory) {
            val files = dir.listFiles()
            for (file in files!!) {
                if (file.isFile) {
                    file.delete()
                    count++
                }
            }
        }
        return count

    }

    fun deleteUnderDir(dirPath: String): Int {
        val dir = File(dirPath)
        var count = 0
        if (dir.isDirectory) {
            val files = dir.listFiles()
            for (file in files!!) {
                if (file.isDirectory) {
                    count += deleteUnderDir(file.absolutePath)
                    file.delete()
                } else {
                    file.delete()
                    count++
                }
            }
        }
        return count

    }

    fun writeToFile(content: String,dir: File,file: String): File {
        //create dependencyFile
        val dependencyFile = File(dir, file)
        dependencyFile.createNewFile()

        IOUtils.write(content, FileOutputStream(dependencyFile), "utf8")

        println("【WRITE】write file # $dependencyFile")
        return dependencyFile
    }



}