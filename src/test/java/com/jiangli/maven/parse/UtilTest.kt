package com.jiangli.maven.parse

import com.jiangli.maven.parse.Util.getCurJarParentDir
import com.jiangli.maven.parse.Util.getCurrentDiskSymbol
import com.jiangli.maven.parse.Util.getMavenPath
import org.junit.Test

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