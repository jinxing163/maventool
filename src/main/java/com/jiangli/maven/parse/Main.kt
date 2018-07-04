package com.jiangli.maven.parse

import com.jiangli.maven.parse.Util.getCurJarParentDir
import com.jiangli.maven.parse.cmd.Cmd
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

    @Autowired
    private val cmdList: List<Cmd>? = null

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
            println("args:${Arrays.toString(args)}")
            println("getBaseJarPath:${getCurJarParentDir()}")
            println("ApplicationHome source:${ApplicationHome(javaClass).source}")
            println("【CONFIG】config origin:$config")
            println("cmd list:$cmdList")

            //read external properties
            processor!!.postProcess(config!!, getCurJarParentDir(),"config.properties")
            println("【CONFIG】config merged:$config")

            Util.resolveProps(config)
            println("【CONFIG】config resolved:$config")

            cmdList?.filter { it.getCmd().equals(config.cmd) }?.forEach {
                println("【PROCESS】find cmd processor:${config.cmd} -> $it")

                it.process(config!!)
            }
        }
    }


}

fun main(args: Array<String>) {
    SpringApplication.run(StartCls::class.java, *args)
}