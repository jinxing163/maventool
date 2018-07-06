package com.jiangli.maven.parse.cmd.version

import java.text.SimpleDateFormat
import java.util.*

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
    fun isDateVersion() :Boolean {
        return isDateVersion(this.str)
    }


    open val format = "yyyyMMdd"

    private fun isDateVersion(str:String) :Boolean {
        try {
            //20180702
            //yyyyMMdd

            if (!str.contains(".") && (str.length > format.length)) {
                val split1 = str.substring(0, format.length)
                SimpleDateFormat(format).parse(split1)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun addLeftPoint(str:String,i:Int): String {
        var ret = str
        (0 until i).forEach {
            ret = "0."+ret
        }
        return ret
    }

    fun addLeftZero(str:String,i:Int): String {
        var ret = str
        if (str.length < i ) {
            (0 until i-str.length).forEach {
                ret = "0"+ ret
            }
        }
        return  ret
    }

    fun removeLeftPoint(str:String,i:Int): String {
        val split1 = str.split("\\.".toRegex())
        return split1.subList(i,split1.size).joinToString(".")
    }

    fun next(offset: VersionDto): VersionDto {
        var thisVerStr = this.str

        //2018070202 -> 20180702.02
        val isDateVer = isDateVersion()
        if (isDateVer) {
            var hisDay = thisVerStr.substring(0, format.length)
            val today = SimpleDateFormat(format).format(Date())
            var dayNum = thisVerStr.substring(format.length).toInt()
            if (hisDay!=today) {
                dayNum = 0
                hisDay = today
            }

            //20180702.2
            thisVerStr = "${hisDay}.${dayNum}"
        }

        //[20180702,02]
        //[1,2,3]
        val split1 = thisVerStr.split("\\.".toRegex())
        var split2 = offset.str.split("\\.".toRegex())

        // 1.2.3 0.1
        if (split1.size < split2.size) {
            split2 = removeLeftPoint(offset.str,split2.size - split1.size).split("\\.".toRegex())
        }
        else if (split1.size > split2.size) {
            split2 = addLeftPoint(offset.str,split1.size - split2.size).split("\\.".toRegex())
        }

        //2.0.4
        val list = mutableListOf<String>()
        split1.forEachIndexed({ index, s ->
            if (isNumber(split1[index]) && isNumber(split2[index])) {
                val nextCalcNum = split1[index].toInt() + split2[index].toInt()

                var nextCalcStr = if (isDateVer && index == split1.lastIndex) {
                    addLeftZero(nextCalcNum.toString(),2)
                } else nextCalcNum.toString()

                list.add(nextCalcStr)
            }
        })

        return if (isDateVer)  VersionDto(list.joinToString(""))
        else  VersionDto(list.joinToString("."))
    }

    fun carryBit(offset: VersionDto): VersionDto {
        val split1 = this.str.split("\\.".toRegex())
        var split2 = offset.str.split("\\.".toRegex())
        if (split1.size != split2.size) {
            return this
        }

        val list = mutableListOf<String>()
        var carry = split1.lastIndex+1
        var carryVal = 0
        (split1.lastIndex downTo 0 ).forEach {
            index ->
            if (isNumber(split1[index]) && isNumber(split2[index])) {
                var max = split2[index].toInt()
                val rs = split1[index].toInt() + carryVal
                carryVal = 0

                if (rs >= max && max > 0 ) {
                    carry = index - 1
                    carryVal = 1
                }
            }
        }

        (0 .. carry-1).forEach {
            index ->
            if (isNumber(split1[index]))
                list.add(split1[index])
        }

        if (carry <= split1.lastIndex && carry >= 0) {
            if (isNumber(split1[carry]))
                list.add((split1[carry].toInt()+1).toString())
        }

        (carry+1 .. split1.lastIndex).forEach {
            index ->
                list.add("0")
        }

        return VersionDto(list.joinToString("."))
    }

    override fun equals(other: Any?): Boolean {
        if (other is VersionDto) {
            return other.str == this.str
        }
        return super.equals(other)
    }
}

fun main(args: Array<String>) {
    println(VersionDto("2018031402").carryBit(VersionDto("0.10.20")))
    println(VersionDto("20180101").next(VersionDto("0.0.1")))
    println(VersionDto("20180101").next(VersionDto("1")))
    println(VersionDto("2018031402").next(VersionDto("0.0.1")))
    println(VersionDto("2018031402").next(VersionDto("1")))
    println(VersionDto("1.2.3").next(VersionDto("0.1")))
    println(VersionDto("1.2.3").next(VersionDto("0.0.1")))
    println(VersionDto("1.2.3").next(VersionDto("1.2.1")))
    println(VersionDto("1.2.3").carryBit(VersionDto("0.10.20")))
    println(VersionDto("1.2.19").carryBit(VersionDto("0.10.20")))
    println(VersionDto("1.2.20").carryBit(VersionDto("0.10.20")))
    println(VersionDto("1.2.21").carryBit(VersionDto("0.10.20")))
    println(VersionDto("1.2.30").carryBit(VersionDto("0.10.20")))
    println(VersionDto("1.9.2").carryBit(VersionDto("0.10.20")))
    println(VersionDto("1.9.19").carryBit(VersionDto("0.10.20")))
    println(VersionDto("1.9.20").carryBit(VersionDto("0.10.20")))
    println(VersionDto("2.8.20").carryBit(VersionDto("0.10.20")))
    println(VersionDto("999.9.20").carryBit(VersionDto("0.10.20")))

//    (5 downTo 3).forEach {
//        println(it)
//    }
//    (0 until  3).forEach {
//        println(it)
//    }
//    (0 .. 3).forEach {
//        println(it)
//    }
}
