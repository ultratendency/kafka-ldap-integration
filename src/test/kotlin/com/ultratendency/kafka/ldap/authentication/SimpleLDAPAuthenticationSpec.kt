package com.ultratendency.kafka.ldap.authentication

import com.ultratendency.kafka.ldap.common.InMemoryLDAPServer
import com.ultratendency.kafka.ldap.common.LDAPCache
import org.amshove.kluent.shouldBeEqualTo
import org.apache.kafka.common.security.plain.PlainAuthenticateCallback
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import javax.security.auth.callback.NameCallback

object SimpleLDAPAuthenticationSpec : Spek(
    {
        describe("SimpleLDAPAuthentication test specifications") {

            beforeGroup {
                InMemoryLDAPServer.start()
                LDAPCache.invalidateAllBinds()
            }

            context("authentication should work correctly") {

                // kind of misuse of the prompt field in NameCallback... Ok in test context
                val tests = mapOf(
                    arrayOf(
                        NameCallback("invalid user and pwd", "dontexist"),
                        PlainAuthenticateCallback("wrong".toCharArray())
                    ) to false,
                    arrayOf(
                        NameCallback("correct user and pwd", "igroup"),
                        PlainAuthenticateCallback("itest".toCharArray())
                    ) to true,
                    arrayOf(
                        NameCallback("correct user and invalid pwd", "igroup"),
                        PlainAuthenticateCallback("wrong".toCharArray())
                    ) to false,
                    arrayOf(
                        NameCallback("correct user and pwd", "srvp01"),
                        PlainAuthenticateCallback("srvp01".toCharArray())
                    ) to true
                )

                tests.forEach { callbacks, result ->
                    val user = (callbacks.first() as NameCallback).defaultName
                    val pwd = (callbacks.last() as PlainAuthenticateCallback).password()

                    it("should for $user with $pwd return $result") {
                        SimpleLDAPAuthentication().handle(callbacks)
                        (callbacks.last() as PlainAuthenticateCallback)
                            .authenticated() shouldBeEqualTo result
                    }
                }
            }

            afterGroup {
                InMemoryLDAPServer.stop()
            }
        }
    }
)
