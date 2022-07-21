package com.ultratendency.kafka.ldap.authentication

import com.ultratendency.kafka.ldap.LDAPConfig
import com.ultratendency.kafka.ldap.common.InMemoryLDAPServer
import com.ultratendency.kafka.ldap.common.LDAPCache
import com.ultratendency.kafka.ldap.toUserDNNodes
import org.amshove.kluent.shouldEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object LDAPAuthenticationSpec : Spek(
    {
        describe("LDAPAuthentication class test specifications") {

            /**
             * Test scope
             * - Test correct and incorrect users in ServiceAccounts
             * - Test correct and incorrect users in ApplAccounts
             *
             * Good enough testing
             *
             * NOT testing a lot of different wrong configurations in yaml
             * invalid host, port, usrBaseDN, usrUid, ...
             * Those will return false anyway
             */

            beforeGroup {
                InMemoryLDAPServer.start()
                LDAPCache.invalidateAllBinds()
            }

            // users from both nodes, ServiceAccounts and ApplAccounts

            val refUsers = mapOf(
                Pair("srvp01", "srvp01") to true,
                Pair("srvc01", "srvc01") to true,
                Pair("srvp01", "invalidpwd") to false,
                Pair("invalid", "srvc01") to false
            )

            context("correct path to default YAML config") {
                refUsers.forEach { user, result ->
                    it("should return $result for user ${user.first} with pwd ${user.second}") {
                        val src = "src/test/resources/ldapconfig.yaml"
                        val userDNs = LDAPConfig.getBySource(src).toUserDNNodes(user.first)

                        LDAPAuthentication.init(src)
                            .canUserAuthenticate(userDNs, user.second)
                            .isNotEmpty() shouldEqual result
                    }
                }
            }

            context("classpath to YAML config") {
                refUsers.forEach { user, result ->
                    it("should return $result for user ${user.first} with pwd ${user.second}") {
                        val userDNs = LDAPConfig.getByClasspath().toUserDNNodes(user.first)

                        LDAPAuthentication.init()
                            .canUserAuthenticate(userDNs, user.second)
                            .isNotEmpty() shouldEqual result
                    }
                }
            }

            afterGroup {
                InMemoryLDAPServer.stop()
            }
        }
    }
)
