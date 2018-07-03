package com.jiangli.maven.parse.cmd.version

/**
 *
 *
 * @author Jiangli
 * @date 2018/4/26 11:10
 */
data class VersionDto(val str: String):Comparable<VersionDto>{
    override fun compareTo(other: VersionDto): Int {
        if (isNumberVersion(this.str) && !isNumberVersion(other.str)) {
            return -1
        } else if (!isNumberVersion(this.str) && isNumberVersion(other.str)) {
            return 1
        }

        val split1 = this.str.split("\\.".toRegex())
        val split2 = other.str.split("\\.".toRegex())
        if (split1.size != split2.size) {
            return split2.size.compareTo(split1.size)
        } else {
            split1.forEachIndexed({ index, s ->
                if (isNumber(split1[index]) && !isNumber(split2[index])) {
                    return -1
                } else if (!isNumber(split1[index]) && isNumber(split2[index])) {
                    return 1
                } else if (!isNumber(split1[index]) && !isNumber(split2[index])) {
                    return 0
                }

                val cmp = split2[index].toInt().compareTo(split1[index].toInt())
                if (cmp!=0 ) {
                    return cmp
                }
            })
        }
        return 0
    }

    private fun isNumber(str:String) :Boolean {
        try {
            str.toInt()
            return true
        } catch (e: Exception) {
//            e.printStackTrace()
        }
        return false
    }
    private fun isNumberVersion(str:String) :Boolean {
        try {
            val split1 = str.split("\\.".toRegex())
            split1.forEach { t: String? ->
                if (!isNumber(t!!)) {
                    return false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }
     fun isNumberVersion() :Boolean {
        return isNumberVersion(this.str)
    }

    fun addLeftPoint(str:String,i:Int): String {
        var ret = str
        (0 until i).forEach {
            ret = "0."+ret
        }
        return ret
    }

    fun removeLeftPoint(str:String,i:Int): String {
        val split1 = str.split("\\.".toRegex())
        return split1.subList(i,split1.size).joinToString(".")
    }

    fun next(offset: VersionDto): VersionDto {
        val split1 = this.str.split("\\.".toRegex())
        var split2 = offset.str.split("\\.".toRegex())

        // 20180101 0.0.1
        // 1.2.3 0.1
        if (split1.size < split2.size) {
            split2 = removeLeftPoint(offset.str,split2.size - split1.size).split("\\.".toRegex())
        }
        else if (split1.size > split2.size) {
            split2 = addLeftPoint(offset.str,split1.size - split2.size).split("\\.".toRegex())
        }

        val list = mutableListOf<String>()
        split1.forEachIndexed({ index, s ->
            if (isNumber(split1[index]) && isNumber(split2[index])) {
                list.add((split1[index].toInt()+split2[index].toInt()).toString())
            }
        })
        return VersionDto(list.joinToString("."))
    }
}

fun main(args: Array<String>) {
    println(VersionDto("20180101").next(VersionDto("0.0.1")))
    println(VersionDto("20180101").next(VersionDto("1")))
    println(VersionDto("1.2.3").next(VersionDto("0.1")))
    println(VersionDto("1.2.3").next(VersionDto("0.0.1")))
    println(VersionDto("1.2.3").next(VersionDto("1.2.1")))
}
