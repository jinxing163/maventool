package com.jiangli.maven.parse

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 *
 *
 * @author Jiangli
 * @date 2018/4/24 16:48
 */
@ConfigurationProperties(prefix = "config")
open class Config {
    var cmd: java.lang.String? = null

    var urlprefix: java.lang.String? = "http://maven.i.zhihuishu.com:8081/nexus/service/local/repositories/thirdparty/content" as java.lang.String
    var groupId: java.lang.String? = null
    var artifactId: java.lang.String? = null
    var retrieveIdx: java.lang.Integer? = 1 as java.lang.Integer
    var nextAddOffset: java.lang.String? = "0.0.1" as java.lang.String
    var defaultVersion: java.lang.String? = "1.0.0" as java.lang.String
    var weightOfEach: java.lang.String? = "0.10.20" as java.lang.String

    override fun toString(): String {
        return "Config(cmd=$cmd, urlprefix=$urlprefix, groupId=$groupId, artifactId=$artifactId, retrieveIdx=$retrieveIdx, nextAddOffset=$nextAddOffset, defaultVersion=$defaultVersion, weightOfEach=$weightOfEach)"
    }


}