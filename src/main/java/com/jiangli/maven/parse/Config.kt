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
    var artifaceId: java.lang.String? = null
    var retreiveIdx: java.lang.Integer? = null
    var nextAddOffset: java.lang.String? = null
    var defaultVersion: java.lang.String? = null

    override fun toString(): String {
        return "Config(cmd=$cmd, urlprefix=$urlprefix, groupId=$groupId, artifaceId=$artifaceId, retreiveIdx=$retreiveIdx, nextAddOffset=$nextAddOffset, defaultVersion=$defaultVersion)"
    }


}