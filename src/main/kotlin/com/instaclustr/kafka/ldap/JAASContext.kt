package com.instaclustr.kafka.ldap

import org.slf4j.LoggerFactory

/**
 * A singleton object for holding jaas username and password credentials
 */

object JAASContext {

    private val log = LoggerFactory.getLogger(JAASContext::class.java)

    var username: String = ""
        get() {
            log.debug("JAASContext: get username '$field'")
            return field
        }
        set(value) {
            log.debug("JAASContext: set username '$value'")
            field = value
        }

    var password: String = ""
        get() {
            log.debug("JAASContext: get password called")
            return field
        }
        set(value) {
            log.debug("JAASContext: set password called")
            field = value
        }
}