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
fun createCurrentVersionFile(destDir:File,it: Config): VersionDto {
    val nextVersion = requestForCurrentVersion(it)
    File(destDir, "${nextVersion.str}.version").createNewFile()
    return nextVersion
}

fun requestForNextVersion(it: Config): VersionDto {
    var nextVersion: VersionDto

    val currentVersion = requestForCurrentVersion(it)
    if (currentVersion == VersionDto(it.defaultVersion.toString())) {
        nextVersion = currentVersion
    } else {
        nextVersion = currentVersion.next(VersionDto(it.nextAddOffset.toString()))

        //check carry bit
        nextVersion = nextVersion.carryBit(VersionDto(it.weightOfEach.toString()))
    }

    return nextVersion
}
fun requestForCurrentVersion(it: Config): VersionDto {
    var currentVersion: VersionDto? = null

    val versionList = requestForCurrentVersions(it)

    currentVersion = if (versionList.isEmpty()) {
        VersionDto(it.defaultVersion.toString())
    } else {
        val retrieveIdx = it.retrieveIdx!!.toInt() - 1
        val filterList = versionList.filter { it.isNumberVersion() }
        if (retrieveIdx < filterList.size) {
            filterList[retrieveIdx]
        } else {
            VersionDto(it.defaultVersion.toString())
        }
    }

    return currentVersion
}

private fun requestForCurrentVersions(it: Config): MutableList<VersionDto> {
    var requestUrl = "${it.urlprefix}/${getMavenPath(it)}"
    println("request for info:$requestUrl")
    //                requestUrl = requestUrl.replace("//", "/")

    val request = Feign.builder()
            //                        .encoder(JAXBEncoder())
            //                        .decoder(JAXBDecoder(JAXBContextFactory.Builder().build()))
            .target(NexusMvnXmlRequest::class.java!!, requestUrl)

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