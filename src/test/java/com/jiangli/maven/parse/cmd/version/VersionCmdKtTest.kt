package com.jiangli.maven.parse.cmd.version

import com.jiangli.maven.parse.Config
import org.junit.Test

/**
 * @author Jiangli
 * @date 2018/7/5 13:29
 */
class VersionCmdKtTest {

    @Test
    fun requestForNextVersion() {
        val config = Config()
//        com.zhihuishu.aries.run
//        openapi-appserver
        config.groupId="com.zhihuishu.qa" as java.lang.String
        config.artifactId ="appsteacher-courseqa" as java.lang.String
        println(requestForCurrentVersion(config))
        println(requestForNextVersion(config))
    }

    @Test
    fun requestForNextVersion2() {
        val config = Config()
//        com.zhihuishu.aries.run
//        openapi-appserver
        config.groupId="com.zhihuishu.aries.run" as java.lang.String
        config.artifactId ="openapi-appserver3" as java.lang.String
        println(requestForCurrentVersion(config))
        println(requestForNextVersion(config))
    }

    @Test
    fun requestForCurrentVersion() {
    }
}