package com.jiangli.maven.parse

import org.springframework.boot.ApplicationHome
import java.io.File

/**
 *
 *
 * @author Jiangli
 * @date 2018/7/3 16:14
 */

object Util{
    /**
     * 获取当前jar包所在系统中的目录
     */
    fun getBaseJarPath(): File {
        val home = ApplicationHome(javaClass)
        val jarFile = home.source
        return jarFile.parentFile
    }
}