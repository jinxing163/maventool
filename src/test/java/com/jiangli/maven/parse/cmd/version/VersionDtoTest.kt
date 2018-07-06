package com.jiangli.maven.parse.cmd.version

import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Jiangli
 * @date 2018/7/6 13:23
 */
class VersionDtoTest {

    @Test
    fun test_isDateVersion() {
        assert(VersionDto("2018031402").isDateVersion())
        assert(VersionDto("2018171402").isDateVersion())
        assert(!VersionDto("20181214.02").isDateVersion())
        assert(!VersionDto("1.2.3").isDateVersion())
    }

    @Test
    fun test_next() {
        val todayStr = SimpleDateFormat(VersionDto("").format).format(Date())
        println(todayStr)
        assert(VersionDto("2018031402").next(VersionDto("1")) == VersionDto("${todayStr}01"))
        assert(VersionDto("2018031402").next(VersionDto("0.0.1")) == VersionDto("${todayStr}01"))
        assert(VersionDto("2018031402").next(VersionDto("0.0.2")) == VersionDto("${todayStr}02"))
        assert(VersionDto("${todayStr}01").next(VersionDto("0.0.1")) == VersionDto("${todayStr}02"))
        assert(VersionDto("${todayStr}01").next(VersionDto("0.0.5")) == VersionDto("${todayStr}06"))
        assert(VersionDto("${todayStr}99").next(VersionDto("0.0.1")) == VersionDto("${todayStr}100"))
        assert(VersionDto("1.2.3").next(VersionDto("0.1")) == VersionDto("1.2.4"))
        assert(VersionDto("1.2.3").next(VersionDto("0.0.1")) == VersionDto("1.2.4"))
        assert(VersionDto("1.2.3").next(VersionDto("1.2.1")) == VersionDto("2.4.4"))
    }

    @Test
    fun test_carryBit() {
        assert(VersionDto("2018031402").carryBit(VersionDto("0.10.20")) == VersionDto("2018031402"))
        assert(VersionDto("2018031402").carryBit(VersionDto("0")) == VersionDto("2018031402"))
        assert(VersionDto("2018031402").carryBit(VersionDto("20")) == VersionDto("0"))
        assert(VersionDto("1.2.3").carryBit(VersionDto("0.10.20")) == VersionDto("1.2.3"))
        assert(VersionDto("1.2.19").carryBit(VersionDto("0.10.20")) == VersionDto("1.2.19"))
        assert(VersionDto("1.2.20").carryBit(VersionDto("0.10.20")) == VersionDto("1.3.0"))
        assert(VersionDto("1.2.21").carryBit(VersionDto("0.10.20")) == VersionDto("1.3.0"))
        assert(VersionDto("1.2.30").carryBit(VersionDto("0.10.20")) == VersionDto("1.3.0"))
        assert(VersionDto("1.9.2").carryBit(VersionDto("0.10.20")) == VersionDto("1.9.2"))
        assert(VersionDto("1.9.19").carryBit(VersionDto("0.10.20")) == VersionDto("1.9.19"))
        assert(VersionDto("1.9.20").carryBit(VersionDto("0.10.20")) == VersionDto("2.0.0"))
        assert(VersionDto("2.8.20").carryBit(VersionDto("0.10.20")) == VersionDto("2.9.0"))
        assert(VersionDto("999.9.20").carryBit(VersionDto("0.10.20")) == VersionDto("1000.0.0"))
    }
}