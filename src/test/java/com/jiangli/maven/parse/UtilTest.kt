package com.jiangli.maven.parse

import com.jiangli.maven.parse.Util.getCurJarParentDir
import com.jiangli.maven.parse.Util.getCurrentDiskSymbol
import com.jiangli.maven.parse.Util.getEnv
import com.jiangli.maven.parse.Util.getMavenPath
import com.jiangli.maven.parse.cmd.init.InitConfig
import com.jiangli.maven.parse.cmd.init.InitJSONConfig
import org.junit.Test
import org.springframework.util.PropertyPlaceholderHelper
import java.util.*

/**
 * @author Jiangli
 * @date 2018/7/3 16:43
 */
class UtilTest {

    @Test
    fun test_getBaseJarPath() {
        println(getCurJarParentDir())
    }

    @Test
    fun test_str() {
       val a:String = "aaa"
        val b:java.lang.String =  a as java.lang.String
        println(b)
    }

    @Test
    fun test_StringValueResolver() {
//        val x =  PropertyPlaceholderConfigurer()
        val x =  PropertyPlaceholderHelper("\${","}",":",false)
        val properties = Properties()
        properties.put("aa","bb是多少")
        val s = x.replacePlaceholders("aaa bbb \${aa}", properties)
        println(s)

        val env = getEnv()
        properties.putAll(env)

        val initJSONConfig = InitJSONConfig()
        initJSONConfig.project_path = "啊啊啊\${jarPath}bbb"
        val mutableListOf = mutableListOf<InitConfig>()
        val element = InitConfig()
        element.jarName="jarNamejarName\${jarPath}jarNamejarName"
        mutableListOf.add(element)
        initJSONConfig.configs = mutableListOf
        val xd = x.replacePlaceholders("aaa bbb \${aa}", properties)
        println(xd)

        for (declaredField in InitJSONConfig::class.java.declaredFields) {
            println(declaredField)
            println(declaredField.type == String::class.java)
            println(declaredField.type == List::class.java)
            declaredField.isAccessible = true
            println(declaredField.get(initJSONConfig))
        }

        println(initJSONConfig)
        Util.resolveProps(initJSONConfig)
        println(initJSONConfig)
    }


    @Test
    fun test_getMavenPath() {
        val config = Config()
//        com.zhihuishu.aries.run
//        openapi-appserver
        config.groupId="com.zhihuishu.aries.run" as java.lang.String
        config.artifactId ="openapi-appserver" as java.lang.String
        println(getMavenPath(config))
    }

    @Test
    fun test_getCurrentDiskSymbol() {
        println(getCurrentDiskSymbol())
    }
}