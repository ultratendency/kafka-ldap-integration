package com.instaclustr.kafka.ldap

import org.slf4j.LoggerFactory

/**
 * A singleton object for getting username and password from KafkaServer JAAS context
 * This object is only valid on kafka broker running PLAIN SASL
 */

object JAASContext {

    private val log = LoggerFactory.getLogger(JAASContext::class.java)

    // extracting JAAS context from kafka server - prerequisite is  PLAINSASL context
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