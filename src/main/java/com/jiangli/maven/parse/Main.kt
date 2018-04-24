package com.jiangli.maven.parse

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationHome
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import java.io.File
import java.util.*



//@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@SpringBootApplication()
@EnableConfigurationProperties(Config::class)
@PropertySource("classpath:configDefault.properties")
open class StartCls {
    @Autowired
    private val config: Config? = null

    @Bean
    open fun commandLineRunner(ctx: ApplicationContext): CommandLineRunner {
        return CommandLineRunner { args ->
            println(Arrays.toString(args))
            println(config)
            println(getBaseJarPath())
//            println(config?.cmd)
//            println(config?.urlprefix)

            config?.let {
                var realUrl = "${it.urlprefix}/${it.groupId?.replaceAll("\\.", "/")}/${it.artifaceId?.replaceAll("\\.", "/")}"
                realUrl = realUrl.replace("//", "/")
//                println(realUrl)


                getBaseJarPath().listFiles().filter { it.isFile && it.name.matches("""\d+\.\d+\.\d+.version""".toRegex()) }.forEach {
                    it.delete()
                }
                
                File(getBaseJarPath(),"1.2.3.version").createNewFile()

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