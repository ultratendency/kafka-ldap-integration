package com.ultratendency.kafka.ldap.authorization

import com.ultratendency.kafka.ldap.JAASContext
import com.ultratendency.kafka.ldap.common.InMemoryLDAPServer
import kafka.security.auth.Acl
import kafka.security.auth.Operation
import kafka.security.auth.PermissionType
import org.amshove.kluent.shouldEqualTo
import org.apache.kafka.common.acl.AclOperation
import org.apache.kafka.common.security.auth.KafkaPrincipal
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.UUID

object GroupAuthorizerSpec : Spek({

    // create read allowance for ldap group
    fun cReadAS(ldapGroup: String): Set<Acl> =
        setOf(
            Acl(
                KafkaPrincipal(KafkaPrincipal.USER_TYPE, ldapGroup),
                PermissionType.fromString("Allow"),
                "*",
                Operation.fromJava(AclOperation.READ)
            )
        )

    // create describe allowance for ldap group
    fun cDescribeAS(ldapGroup1: String, ldapGroup2: String): Set<Acl> =
        setOf(
            Acl(
                KafkaPrincipal(KafkaPrincipal.USER_TYPE, ldapGroup1),
                PermissionType.fromString("Allow"),
                "*",
                Operation.fromJava(AclOperation.DESCRIBE)
            ),
            Acl(
                KafkaPrincipal(KafkaPrincipal.USER_TYPE, ldapGroup2),
                PermissionType.fromString("Allow"),
                "*",
                Operation.fromJava(AclOperation.DESCRIBE)
            )
        )

    // create write allowance for ldap group
    fun cWriteAS(ldapGroup: String): Set<Acl> =
        setOf(
            Acl(
                KafkaPrincipal(KafkaPrincipal.USER_TYPE, ldapGroup),
                PermissionType.fromString("Allow"),
                "*",
                Operation.fromJava(AclOperation.WRITE)
            )
        )

    // helper function for creating KafkaPrincipal
    fun createKP(userName: String): KafkaPrincipal =
        KafkaPrincipal(KafkaPrincipal.USER_TYPE, userName)

    // set the JAAS config in order to do successful init of LDAPAuthorization
    JAASContext.username = "igroup"
    JAASContext.password = "itest"

    describe("GroupAuthorizer class test specifications") {

        beforeGroup {
            InMemoryLDAPServer.start()
        }

        val refUserDescribeACL = mapOf(
            Triple("srvp01", listOf("KC-tpc-01", "KP-tpc-01"), "tpc-01") to false,
            Triple("srvc01", listOf("KC-tpc-01", "KP-tpc-01"), "tpc-01") to false,

            Triple("srvp01", listOf("KC-tpc-02", "KP-tpc-02"), "tpc-02") to true,
            Triple("srvc01", listOf("KC-tpc-02", "KP-tpc-02"), "tpc-02") to false,

            Triple("srvp01", listOf("KC-tpc-03", "KP-tpc-03"), "tpc-03") to false,
            Triple("srvc01", listOf("KC-tpc-03", "KP-tpc-03"), "tpc-03") to true
        )

        val refUserWriteACL = mapOf(
            Triple("srvp01", "KP-tpc-01", "tpc-01") to false,
            Triple("srvp01", "KP-tpc-02", "tpc-02") to true,
            Triple("srvp01", "KP-tpc-03", "tpc-03") to false
        )

        val refUserReadACL = mapOf(
            Triple("srvc01", "KC-tpc-01", "tpc-01") to false,
            Triple("srvc01", "KC-tpc-02", "tpc-02") to false,
            Triple("srvc01", "KC-tpc-03", "tpc-03") to true
        )

        context("describe allowance") {
            refUserDescribeACL.forEach { tr, result ->
                it(
                    "should return $result for user ${tr.first} trying describe " +
                        "on topic ${tr.third}"
                ) {
                    GroupAuthorizer(UUID.randomUUID().toString())
                        .authorize(
                            createKP(tr.first),
                            cDescribeAS(tr.second.first(), tr.second.last())
                        ) shouldEqualTo result
                }
            }
        }

        context("write allowance") {
            refUserWriteACL.forEach { tr, result ->
                it(
                    "should return $result for user ${tr.first} trying write on " +
                        "topic ${tr.third}"
                ) {
                    GroupAuthorizer(UUID.randomUUID().toString())
                        .authorize(
                            createKP(tr.first),
                            cWriteAS(tr.second)
                        ) shouldEqualTo result
                }
            }
        }

        context("read allowance") {
            refUserReadACL.forEach { tr, result ->
                it(
                    "should return $result for user ${tr.first} trying read on " +
                        "topic ${tr.third}"
                ) {
                    GroupAuthorizer(UUID.randomUUID().toString())
                        .authorize(
                            createKP(tr.first),
                            cReadAS(tr.second)
                        ) shouldEqualTo result
                }
            }
        }

        afterGroup {
            InMemoryLDAPServer.stop()
        }
    }
})
