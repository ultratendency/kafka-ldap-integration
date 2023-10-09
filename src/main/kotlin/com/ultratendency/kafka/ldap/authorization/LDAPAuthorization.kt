package com.ultratendency.kafka.ldap.authorization

import com.ultratendency.kafka.ldap.JAASContext
import com.ultratendency.kafka.ldap.LDAPConfig
import com.ultratendency.kafka.ldap.Monitoring
import com.ultratendency.kafka.ldap.common.LDAPBase
import com.ultratendency.kafka.ldap.toAdminDN
import com.unboundid.ldap.sdk.Filter
import com.unboundid.ldap.sdk.LDAPException
import com.unboundid.ldap.sdk.LDAPSearchException
import com.unboundid.ldap.sdk.SearchRequest
import com.unboundid.ldap.sdk.SearchScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.measureTimeMillis

/**
 * A class verifying group membership with LDAP
 */
class LDAPAuthorization private constructor(
    private val uuid: String,
    val config: LDAPConfig.Config,
) : LDAPBase(config) {
    // In authorization context, needs to bind the connection before compare-match between group
    // and user due to no anonymous access allowed for LDAP operations like search, compare, ...
    private val connectionAndBindIsOk: Boolean

    init {
        connectionAndBindIsOk =
            when {
                JAASContext.username.isEmpty() || JAASContext.password.isEmpty() -> false
                !ldapConnection.isConnected -> false
                else -> doBind(config.toAdminDN(JAASContext.username), JAASContext.password)
            }
    }

    private fun doBind(
        userDN: String,
        pwd: String,
    ): Boolean =
        try {
            log.debug(
                "Binding information for authorization fetched from JAAS config file [$userDN]",
            )
            measureTimeMillis { ldapConnection.bind(userDN, pwd) }
                .also {
                    log.debug("Successfully bind to (${config.host},${config.port}) with $userDN")
                    log.info("${Monitoring.AUTHORIZATION_BIND_TIME.txt} $it")
                }
            true
        } catch (e: LDAPException) {
            log.error(
                "${Monitoring.AUTHORIZATION_BIND_FAILED.txt} $userDN to (${config.host}," +
                    "${config.port}) - ${e.diagnosticMessage}",
            )
            false
        }

    private fun getGroupDN(groupName: String): String =
        try {
            val filter = Filter.createEqualityFilter(config.grpUid, groupName)

            ldapConnection
                .search(
                    SearchRequest(
                        config.grpBaseDN,
                        SearchScope.SUB,
                        filter,
                        SearchRequest.NO_ATTRIBUTES,
                    ),
                )
                .let {
                    if (it.entryCount == 1) {
                        it.searchEntries[0].dn
                    } else {
                        log.error(
                            "${Monitoring.AUTHORIZATION_SEARCH_MISS.txt} $groupName under " +
                                "${config.grpBaseDN} ($uuid)",
                        )
                        ""
                    }
                }
        } catch (e: LDAPSearchException) {
            log.error(
                "${Monitoring.AUTHORIZATION_SEARCH_FAILURE.txt} $groupName under " +
                    "${config.grpBaseDN} ($uuid)",
            )
            ""
        }

    private fun getGroupMembers(groupDN: String): List<String> =
        try {
            if (groupDN.isNotEmpty()) {
                ldapConnection.getEntry(groupDN)
                    ?.getAttributeValues(config.grpAttrName)
                    ?.map { it.lowercase() } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: LDAPException) {
            log.error(
                "${Monitoring.AUTHORIZATION_GROUP_FAILURE.txt} - ${config.grpAttrName} - " +
                    "for $groupDN ($uuid)",
            )
            emptyList()
        }

    override fun isUserMemberOfAny(
        userDNs: List<String>,
        groups: List<String>,
    ): Set<AuthorResult> {
        if (!connectionAndBindIsOk) {
            log.error(
                "${Monitoring.AUTHORIZATION_LDAP_FAILURE.txt} $userDNs membership in " +
                    "$groups ($uuid)",
            )
            return emptySet()
        }

        val matching =
            groups.flatMap { groupName ->
                val groupDN = getGroupDN(groupName)
                val members = getGroupMembers(groupDN)
                log.info("Group $groupDN has members $members, checking for presence of $userDNs")
                members.intersect(userDNs).map { uDN -> AuthorResult(groupName, uDN) }
            }

        log.info("Checking $userDNs for membership in $groups, found: $matching")
        return matching.toSet()
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(LDAPAuthorization::class.java)

        fun init(
            uuid: String,
            configFile: String = "",
        ): LDAPAuthorization =
            when (configFile.isEmpty()) {
                true -> LDAPAuthorization(uuid, LDAPConfig.getByClasspath())
                else -> LDAPAuthorization(uuid, LDAPConfig.getBySource(configFile))
            }
    }
}
