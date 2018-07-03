package com.jiangli.maven.parse.cmd.version

import feign.RequestLine


/**
 *
 *
 * @author Jiangli
 * @date 2018/4/26 10:40
 */
interface NexusMvnXmlRequest {
    // RequestLine注解声明请求方法和请求地址,可以允许有查询参数
    @RequestLine("GET /")
    fun req(): String

//    @RequestLine("GET /")
//    fun req(): Root
}