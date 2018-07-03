package com.jiangli.maven.parse.cmd

import com.jiangli.maven.parse.Config

/**
 *
 *
 * @author Jiangli
 * @date 2018/7/3 16:10
 */
interface  Cmd {
    fun process(config: Config)

    fun getCmd():String
}
abstract class BaseCmd(val cmdName: String) :Cmd {
    override fun getCmd(): String {
        return cmdName
    }
}