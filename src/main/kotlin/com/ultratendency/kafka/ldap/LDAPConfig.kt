package com.ultratendency.kafka.ldap

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import java.net.URL
import java.nio.file.FileSystemNotFoundException
import java.nio.file.Files
import java.nio.file.Paths

/**
 * A singleton class returning a data class for all config parameters
 * The configuration can be loaded in two different ways
 * - by source, used in test scenarios
 * - by classpath, used when running of kafka brokers
 *
 * See test/resources/adconfig.yaml for 1:1 mapping between YAML and data class
 */
object LDAPConfig {
    data class Config(
        val host: String,
        val port: Int,
        val connTimeout: Int,
        val adminBaseDN: String,
        val adminUid: String,
        val usrBaseDN: String,
        val usrUid: String,
        val grpBaseDN: String,
        val grpUid: String,
        val grpAttrName: String,
        val usrCacheExpire: Int,
        val grpCacheExpire: Int,
    )

    private val log = LoggerFactory.getLogger(LDAPConfig::class.java)
    private val cache: Config

    val emptyConfig =
        Config(
            "",
            0,
            0,
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            0,
            0,
        )

    init {
        cache =
            try {
                loadConfig(ClassLoader.getSystemResource("ldapconfig.yaml") ?: URL(""))
                    .also {
                        log.info("LDAPConfig for classpath is cached")
                        log.info("ldap configuration values: $it")
                    }
            } catch (e: Exception) {
                log.error("${e.message} - authentication and authorization will fail! ")
                emptyConfig
            }
    }

    fun getBySource(configFile: String): Config {
        val prefix = if (System.getProperty("os.name").startsWith("Windows")) "file:/" else "file:"
        return loadConfig(URL(prefix + System.getProperty("user.dir") + "/" + configFile))
    }

    fun getByClasspath(): Config = cache

    private fun loadConfig(configFile: URL): Config {
        val mapper = ObjectMapper(YAMLFactory())

        mapper.registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .disable(KotlinFeature.NullIsSameAsDefault)
                .disable(KotlinFeature.SingletonSupport)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build(),
        ) // Enable Kotlin and data class support

        val errMsg = "Authentication and authorization will fail - "
        val defaultDir = Paths.get("").toAbsolutePath()
        val filePath =
            try {
                Paths.get(configFile.toURI())
            } catch (e: IllegalArgumentException) {
                log.error(errMsg + e.message)
                defaultDir
            } catch (e: FileSystemNotFoundException) {
                log.error(errMsg + e.message)
                defaultDir
            } catch (e: SecurityException) {
                log.error(errMsg + e.message)
                defaultDir
            } catch (e: Exception) {
                log.error(errMsg + e.message)
                defaultDir
            }

        if (filePath == defaultDir) return emptyConfig

        return try {
            Files.newBufferedReader(filePath)
                .use {
                    mapper.readValue(it, Config::class.java)
                }
                .also {
                    log.info("$configFile read")
                }
        } catch (e: java.io.IOException) {
            log.error(errMsg + e.message)
            emptyConfig
        } catch (e: SecurityException) {
            log.error(errMsg + e.message)
            emptyConfig
        } catch (e: Exception) {
            log.error(errMsg + e.message)
            emptyConfig
        }
    }
}

// A couple of extension functions for Config
fun LDAPConfig.Config.toUserDN(user: String) = "$usrUid=$user,$usrBaseDN".lowercase()

fun LDAPConfig.Config.toAdminDN(user: String) = "$adminUid=$user,$adminBaseDN".lowercase()

fun LDAPConfig.Config.toUserDNNodes(user: String) = listOf(toUserDN(user), toAdminDN(user))
