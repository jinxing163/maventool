package com.jiangli.maven.parse

import org.apache.commons.io.IOUtils
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 *
 *
 * @author Jiangli
 * @date 2018/7/6 11:10
 */
fun main(args: Array<String>) {
    val projectPath = Util.getCurJarParentDir().parentFile

    val zipName = "自动打jar上传maven库小工具.zip"
    val pkgCmd = "package.bat"
    val list = mutableListOf(
            "使用说明.docx",
            "initScripts.bat",
            "示例pom.xml",
            "conf.json",
            "target/maven_tool.jar"
    )

    val exec = Runtime.getRuntime().exec("${projectPath}/$pkgCmd")
    val inputStream = exec.inputStream
    val thread = Thread(Runnable {
        IOUtils.copy(inputStream,System.out)
    })
    thread.isDaemon = true
    thread.start()

    exec.waitFor()

    ZipUtil.pkg(projectPath,zipName,*list.toTypedArray())
}

object ZipUtil {
    fun pkg(dir:File,zipName:String ,vararg filenames:String){
        //创建zip输出流
        val out = ZipOutputStream(FileOutputStream(File(dir, zipName)));

        //创建缓冲输出流
        val bos = BufferedOutputStream(out)

        filenames.forEach {
            val each = File(dir, it)
            println("$each ${each.exists()}")

            //调用函数
            compress(out, each, each.name);
        }

        bos.close();
        out.close();
    }

    private fun compress(out: ZipOutputStream,  sourceFile: File, base: String) {
        //如果路径为目录（文件夹）
        if (sourceFile.isDirectory()) {

            //取出文件夹中的文件（或子文件夹）
            val flist = sourceFile.listFiles();

            if (flist.size == 0)//如果文件夹为空，则只需在目的地zip文件中写入一个目录进入点
            {
                out.putNextEntry(ZipEntry(base + "/"));
            } else//如果文件夹不为空，则递归调用compress，文件夹中的每一个文件（或文件夹）进行压缩
            {
                (0 until flist.size).forEach { i ->
                    compress(out,  flist[i], base + "/" + flist[i].getName());
                }
            }
        } else//如果不是目录（文件夹），即为文件，则先写入目录进入点，之后将文件写入zip文件中
        {
            out.putNextEntry(ZipEntry(base+""));

            IOUtils.copy(FileInputStream(sourceFile),out)
        }
    }
}

