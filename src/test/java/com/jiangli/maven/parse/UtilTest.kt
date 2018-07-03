package com.jiangli.maven.parse

import com.jiangli.maven.parse.Util.getCurJarParentDir
import com.jiangli.maven.parse.Util.getCurrentDiskSymbol
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
    fun test_getCurrentDiskSymbol() {
        println(getCurrentDiskSymbol())
    }
}