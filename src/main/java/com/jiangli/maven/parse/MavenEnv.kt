package com.jiangli.maven.parse

import org.apache.commons.io.IOUtils
import org.dom4j.io.SAXReader
import java.io.File
import java.nio.charset.Charset



/**
 *
 *
 * @author Jiangli
 * @date 2018/7/6 11:10
 */
fun main(args: Array<String>) {
    findRepoPath()

}

 fun findRepoPath(): String? {
    val exec = Runtime.getRuntime().exec("where mvn.bat")
    val inputStream = exec.inputStream

    val batPath = IOUtils.toString(inputStream, Charset.defaultCharset())
    val batPathOne = batPath.split("\r\n")[0]

    val batFile = File(batPathOne)
    if (batFile.exists()) {
        val MVN_HOME = batFile.parentFile.parentFile
        println("MVN_HOME：$MVN_HOME")
        exec.waitFor()

        val confFile = File(MVN_HOME, "conf/settings.xml")

        val reader = SAXReader()
        val document = reader.read(confFile)
        val settings = document.rootElement
        val element = settings.element("localRepository")
        val REPO_PATH = element.data.toString()
        println("REPO_PATH：$REPO_PATH")

        return REPO_PATH

    } else {
        println("找不到maven path")
    }
    return null
}


