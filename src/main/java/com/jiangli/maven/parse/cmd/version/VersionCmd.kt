package com.jiangli.maven.parse.cmd.version

import com.jiangli.maven.parse.Config
import com.jiangli.maven.parse.Util
import com.jiangli.maven.parse.Util.getCurJarParentDir
import com.jiangli.maven.parse.Util.getMavenPath
import com.jiangli.maven.parse.cmd.BaseCmd
import feign.Feign
import feign.FeignException
import org.dom4j.DocumentHelper
import org.springframework.stereotype.Component
import java.io.File
import java.util.*


//createDependencyFile
fun createDependencyFile(destDir: File,it: Config, nextVersion: VersionDto): File {
    var dependency = """
        <dependency>
            <groupId>${it.groupId}</groupId>
            <artifactId>${it.artifactId}</artifactId>
            <version>${nextVersion.str}</version>
        </dependency>
                        """.trimIndent()

    //create dependencyFile
    return Util.writeToFile(dependency, destDir, "依赖.txt")
}

//create x.version
fun createNextVersionFile(destDir:File,it: Config): VersionDto {
    val nextVersion = requestForNextVersion(it)
    File(destDir, "${nextVersion.str}.version").createNewFile()
    return nextVersion
}

fun requestForNextVersion(it: Config): VersionDto {
    var requestUrl = "${it.urlprefix}/${getMavenPath(it)}"
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

        val retrieveIdx = it.retrieveIdx!!.toInt() - 1
        val filterList = versionList.filter { it.isNumberVersion() }

        nextVersion = if (retrieveIdx < filterList.size) {
            filterList[retrieveIdx].next(VersionDto(it.nextAddOffset.toString()))
        } else {
            VersionDto(it.defaultVersion.toString())
        }
    }
    return nextVersion
}

private fun requestForCurrentVersions(request: NexusMvnXmlRequest): MutableList<VersionDto> {
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

/**
 *
 *
 * @author Jiangli
 * @date 2018/7/3 16:12
 */
@Component
class VersionCmd:BaseCmd("version"){

    override fun process(config: Config) {
        config?.let {

            //delete previous x.version
            getCurJarParentDir().listFiles().filter { it.isFile && it.name.matches(""".*\.version""".toRegex()) }.forEach {
                it.delete()
            }

            //create x.version
            var nextVersion: VersionDto = createNextVersionFile(getCurJarParentDir(),it)
            println("最新版本:" + nextVersion)

            createDependencyFile(getCurJarParentDir(),it, nextVersion)
        }
    }




}