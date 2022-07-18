package com.ultratendency.kafka.ldap.authorization

import com.ultratendency.kafka.ldap.JAASContext
import com.ultratendency.kafka.ldap.LDAPConfig
import com.ultratendency.kafka.ldap.common.InMemoryLDAPServer
import com.ultratendency.kafka.ldap.common.LDAPCache
import com.ultratendency.kafka.ldap.toUserDNNodes
import java.util.UUID
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object LDAPAuthorizationSpec : Spek({

    // set the JAAS config in order to do successful init of LDAPAuthorization
    JAASContext.username = "igroup"
    JAASContext.password = "itest"

    describe("LDAPAuthorization class test specifications") {

        /**
         * Test scope
         * Group membership must be tested for 2 different group - and 2 user levels
         * group levels - AccountGroupNotInRemedy and kafka
         * user levels - ServiceAccounts and ApplAccounts
         *
         * Good enough testing
         *
         * NOT testing a lot of different wrong configurations in yaml
         * invalid host, port, grpBaseDN, ...
         * Those will return 0 anyway
         */

        beforeGroup {
            InMemoryLDAPServer.start()
            LDAPCache.invalidateAllGroups()
        }

        val refUserGroup = mapOf(
            Pair("srvc01", listOf("rmy-01")) to 1,
            Pair("srvp01", listOf("rmy-01", "rmy-02")) to 1,
            Pair("srvp01", listOf("KC-tpc-02", "KP-tpc-02")) to 1
        )

        context("correct path to default YAML config") {
            refUserGroup.forEach { usrGrp, size ->
                it(
                    "should return $size membership(s) for user ${usrGrp.first} in " +
                        "${usrGrp.second}"
                ) {
                    val src = "src/test/resources/ldapconfig.yaml"
                    val userDNs = LDAPConfig.getBySource(src).toUserDNNodes(usrGrp.first)

                    LDAPAuthorization.init(
                        UUID.randomUUID().toString(),
                        src
                    ).isUserMemberOfAny(userDNs, usrGrp.second).size shouldEqual size
                }
            }
        }

        context("classpath to  YAML config") {
            refUserGroup.forEach { usrGrp, size ->
                it(
                    "should return $size membership(s) for user ${usrGrp.first} in" +
                        "${usrGrp.second}"
                ) {

                    val userDNs = LDAPConfig.getByClasspath().toUserDNNodes(usrGrp.first)

                    LDAPAuthorization.init(UUID.randomUUID().toString())
                        .isUserMemberOfAny(userDNs, usrGrp.second).size shouldEqual size
                }
            }
        }

        afterGroup {
            InMemoryLDAPServer.stop()
        }
    }
})
