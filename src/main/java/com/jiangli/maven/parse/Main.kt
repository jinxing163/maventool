package com.jiangli.maven.parse

import com.jiangli.maven.parse.cmd.NexusMvnXmlRequest
import com.jiangli.maven.parse.cmd.VersionDto
import feign.Feign
import org.apache.commons.io.IOUtils
import org.dom4j.DocumentHelper
import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationHome
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.bind.PropertiesConfigurationFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.convert.support.DefaultConversionService
import org.springframework.core.env.MutablePropertySources
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.util.ClassUtils
import org.springframework.util.StringUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*


//@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@SpringBootApplication()
@EnableConfigurationProperties(Config::class)
@PropertySource("classpath:configDefault.properties")
open class StartCls {
    @Autowired
    private val config: Config? = null

    @Autowired
    private val processor: ConfigurationPropertiesBindingPostProcessor? = null


    fun ConfigurationPropertiesBindingPostProcessor.postProcess(bean: Any, dir: File, file: String) {
        val propFile = File(dir, file)
        if (!propFile.exists()) {
            return
        }

        var annotation: ConfigurationProperties = AnnotationUtils
                .findAnnotation(bean.javaClass, ConfigurationProperties::class.java)!!

        val factory = PropertiesConfigurationFactory(bean)
        val mutablePropertySources = MutablePropertySources()

        val properties = Properties()
        properties.load(FileInputStream(propFile))
        mutablePropertySources.addLast(PropertiesPropertySource(file, properties))

        factory.setPropertySources(mutablePropertySources)

//        factory.setValidator(determineValidator(bean))
        factory.setConversionService(DefaultConversionService())
        if (annotation != null) {
            factory.setIgnoreInvalidFields(annotation.ignoreInvalidFields)
            factory.setIgnoreUnknownFields(annotation.ignoreUnknownFields)
            factory.setExceptionIfInvalid(annotation.exceptionIfInvalid)
            factory.setIgnoreNestedProperties(annotation.ignoreNestedProperties)
            if (StringUtils.hasLength(annotation.prefix)) {
                factory.setTargetName(annotation.prefix)
            }
        }
        try {
            factory.bindPropertiesToTarget()
        } catch (ex: Exception) {
            val targetClass = ClassUtils.getShortName(bean.javaClass)
            throw BeanCreationException("bean", "Could not bind properties to "
                    + targetClass, ex)
        }
    }

    @Bean
    open fun commandLineRunner(ctx: ApplicationContext): CommandLineRunner {
        return CommandLineRunner { args ->
            println(Arrays.toString(args))
            println(processor)
            println(getBaseJarPath())


            println(config)
            //read external properties
            processor!!.postProcess(config!!,getBaseJarPath(),"config.properties")
            println(config)

//            println(config?.cmd)
//            println(config?.urlprefix)

            config?.let {
                var realUrl = "${it.urlprefix}/${it.groupId?.replaceAll("\\.", "/")}/${it.artifaceId?.replaceAll("\\.", "/")}"
//                realUrl = realUrl.replace("//", "/")
                println(realUrl)

                val request = Feign.builder()
//                        .encoder(JAXBEncoder())
//                        .decoder(JAXBDecoder(JAXBContextFactory.Builder().build()))
                        .target(NexusMvnXmlRequest::class.java!!, realUrl)

                val xmlStr = request.req()
//                println(xmlStr)

                val document = DocumentHelper.parseText(xmlStr)
                val root = document.getRootElement()
//                println(document)
//                println(root.name)
//                println(rootAttr)

                val allVersions = root.selectNodes("//text")
//                println(allVersions)

                val list = mutableListOf<VersionDto>()
                allVersions.forEach {
//                    println(it.text)
                    list.add(VersionDto(it.text))
                }
                Collections.sort(list)
                println(list)

                val retreiveIdx = it.retreiveIdx!!.toInt()-1
                val filterList = list.filter { it.isNumberVersion() }
                var nextVersion:VersionDto
                nextVersion = if (retreiveIdx < filterList.size) {
                    filterList[retreiveIdx].next(VersionDto(it.nextAddOffset.toString()))
                } else {
                    VersionDto(it.defaultVersion.toString())
                }

                println("最新版本:"+nextVersion)

                //delete
                getBaseJarPath().listFiles().filter { it.isFile && it.name.matches(""".*\.version""".toRegex()) }.forEach {
                    it.delete()
                }

                //create x.version
                File(getBaseJarPath(),"${nextVersion.str}.version").createNewFile()

                var dependency = """
<dependency>
    <groupId>${it.groupId}</groupId>
    <artifactId>${it.artifaceId}</artifactId>
    <version>${nextVersion.str}</version>
</dependency>
                """.trimIndent()

                //create dependencyFile
                val dependencyFile = File(getBaseJarPath(), "依赖.txt")
                dependencyFile.createNewFile()

                IOUtils.write(dependency,FileOutputStream(dependencyFile),"utf8")
            }
        }
    }

    /**
     * 获取当前jar包所在系统中的目录
     */
    fun getBaseJarPath(): File {
        val home = ApplicationHome(javaClass)
        val jarFile = home.source
        return jarFile.parentFile
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(StartCls::class.java, *args)
}