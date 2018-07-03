package com.jiangli.maven.parse.cmd.version

import com.jiangli.maven.parse.Config
import com.jiangli.maven.parse.Util
import com.jiangli.maven.parse.Util.getBaseJarPath
import com.jiangli.maven.parse.cmd.BaseCmd
import feign.Feign
import feign.FeignException
import org.dom4j.DocumentHelper
import org.springframework.stereotype.Component
import java.io.File
import java.util.*

/**
 *
 *
 * @author Jiangli
 * @date 2018/7/3 16:12
 */
@Component
class VersionCmd:BaseCmd("version"){

    fun requestForCurrentVersions(request: NexusMvnXmlRequest): MutableList<VersionDto> {
        val list = mutableListOf<VersionDto>()

        val xmlStr: String
        try {
            xmlStr = request.req()
            val document = DocumentHelper.parseText(xmlStr)
            val root = document.getRootElement()

            val allVersions = root.selectNodes("//text")

            allVersions.forEach {
                list.add(VersionDto(it.text))
            }
            Collections.sort(list)
        } catch (e: FeignException) {
            System.err.print("nexus服务器异常"+e.message)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        return list
    }

    override fun process(config: Config) {
        config?.let {
            var requestUrl = "${it.urlprefix}/${it.groupId?.replaceAll("\\.", "/")}/${it.artifaceId?.replaceAll("\\.", "/")}"
            println("request for info:$requestUrl")
            //                requestUrl = requestUrl.replace("//", "/")

            val request = Feign.builder()
                    //                        .encoder(JAXBEncoder())
                    //                        .decoder(JAXBDecoder(JAXBContextFactory.Builder().build()))
                    .target(NexusMvnXmlRequest::class.java!!, requestUrl)

            var nextVersion: VersionDto? = null

            val versionList = requestForCurrentVersions(request)
            //                println(xmlStr)

            if (versionList.isEmpty()) {
                nextVersion = VersionDto(it.defaultVersion.toString())
                println("当前没有version记录 使用默认版本:$nextVersion")
            } else {
                println("当前有version记录:$versionList")

                val retreiveIdx = it.retreiveIdx!!.toInt() - 1
                val filterList = versionList.filter { it.isNumberVersion() }

                nextVersion = if (retreiveIdx < filterList.size) {
                    filterList[retreiveIdx].next(VersionDto(it.nextAddOffset.toString()))
                } else {
                    VersionDto(it.defaultVersion.toString())
                }
            }

            println("最新版本:" + nextVersion)

            //delete previous x.version
            getBaseJarPath().listFiles().filter { it.isFile && it.name.matches(""".*\.version""".toRegex()) }.forEach {
                it.delete()
            }

            //create x.version
            File(getBaseJarPath(), "${nextVersion.str}.version").createNewFile()

            var dependency = """
    <dependency>
        <groupId>${it.groupId}</groupId>
        <artifactId>${it.artifaceId}</artifactId>
        <version>${nextVersion.str}</version>
    </dependency>
                    """.trimIndent()

            //create dependencyFile
            Util.writeToFile(dependency,getBaseJarPath(),"依赖.txt")
        }
    }
}