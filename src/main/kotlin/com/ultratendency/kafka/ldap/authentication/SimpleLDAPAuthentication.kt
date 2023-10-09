package com.ultratendency.kafka.ldap.authentication

import com.ultratendency.kafka.ldap.JAASContext
import com.ultratendency.kafka.ldap.LDAPConfig
import com.ultratendency.kafka.ldap.Monitoring
import com.ultratendency.kafka.ldap.common.LDAPCache
import com.ultratendency.kafka.ldap.toUserDNNodes
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler
import org.apache.kafka.common.security.plain.PlainAuthenticateCallback
import org.slf4j.LoggerFactory
import java.io.IOException
import javax.security.auth.callback.Callback
import javax.security.auth.callback.NameCallback
import javax.security.auth.callback.UnsupportedCallbackException
import javax.security.auth.login.AppConfigurationEntry

/**
 * A simple class for performing authentication
 * See KIP-86 for details
 * https://cwiki.apache.org/confluence/display/KAFKA/KIP-86%3A+Configurable+SASL+callback+handlers
 *
 * Also see a kind of framework
 * https://github.com/apache/kafka/blob/2.0/clients/src/main/java/org/apache/kafka/common/security
 *  /plain/internals/PlainServerCallbackHandler.java
 */
class SimpleLDAPAuthentication : AuthenticateCallbackHandler {
    init {
        log.debug("${SimpleLDAPAuthentication::class.java.canonicalName} object created")
    }

    private inline fun <reified T> Array<out Callback>.getFirst(): T? =
        this.firstOrNull { it is T } as T

    private inline fun <reified T, reified U> Array<out Callback>.other(): Callback? =
        this.firstOrNull { it !is T && it !is U }

    @Throws(IOException::class, UnsupportedCallbackException::class)
    override fun handle(callbacks: Array<out Callback>?) {
        callbacks?.getFirst<PlainAuthenticateCallback>()?.let { plainCB ->
            val username = callbacks.getFirst<NameCallback>()?.defaultName ?: ""
            val password = plainCB.password().joinToString("")
            plainCB.authenticated(authenticate(username, password))
        }

        callbacks?.other<NameCallback, PlainAuthenticateCallback>()
            ?.let { throw UnsupportedCallbackException(it) }
    }

    private fun authenticate(
        username: String,
        password: String,
    ): Boolean {
        log.debug("Authentication Start - user=$username")

        // always check cache before ldap lookup
        val userDNs = LDAPConfig.getByClasspath().toUserDNNodes(username)
        val isAuthenticated = userInCache(userDNs, password) || userCanBindInLDAP(userDNs, password)
        logAuthenticationResult(isAuthenticated, username)
        return isAuthenticated
    }

    private fun userInCache(
        userDNs: List<String>,
        password: String,
    ): Boolean = userDNs.any { uDN -> LDAPCache.userExists(uDN, password) }

    private fun userCanBindInLDAP(
        userDNs: List<String>,
        password: String,
    ): Boolean =
        LDAPAuthentication.init()
            .use { ldap -> ldap.canUserAuthenticate(userDNs, password) }
            .map { LDAPCache.userAdd(it.userDN, password) }
            .isNotEmpty()

    private fun logAuthenticationResult(
        isAuthenticated: Boolean,
        username: String,
    ) {
        if (isAuthenticated) {
            log.info(
                "${Monitoring.AUTHENTICATION_SUCCESS.txt} - user=$username, status=authenticated",
            )
        } else {
            log.error("${Monitoring.AUTHENTICATION_FAILED.txt} - user=$username, status=denied")
        }
    }

    override fun configure(
        configs: MutableMap<String, *>?,
        saslMechanism: String?,
        jaasConfigEntries: MutableList<AppConfigurationEntry>?,
    ) {
        val jaasOptions = jaasConfigEntries?.get(0)?.options
        JAASContext.username = optionValue(jaasOptions, "username")
        JAASContext.password = optionValue(jaasOptions, "password")
    }

    private fun optionValue(
        jaasOptions: Map<String, *>?,
        option: String,
    ): String {
        val maybeValue = jaasOptions?.get(option)
        if (maybeValue is String) {
            return maybeValue
        }
        return ""
    }

    override fun close() {}

    companion object {
        private val log = LoggerFactory.getLogger(SimpleLDAPAuthentication::class.java)
    }
}
