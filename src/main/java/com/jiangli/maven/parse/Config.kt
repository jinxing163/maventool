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

    var urlprefix: java.lang.String? = null
    var groupId: java.lang.String? = null
    var artifactId: java.lang.String? = null
    var retrieveIdx: java.lang.Integer? = null
    var nextAddOffset: java.lang.String? = null
    var defaultVersion: java.lang.String? = null
    var weightOfEach: java.lang.String? = null

    override fun toString(): String {
        return "Config(cmd=$cmd, urlprefix=$urlprefix, groupId=$groupId, artifactId=$artifactId, retrieveIdx=$retrieveIdx, nextAddOffset=$nextAddOffset, defaultVersion=$defaultVersion, weightOfEach=$weightOfEach)"
    }


}