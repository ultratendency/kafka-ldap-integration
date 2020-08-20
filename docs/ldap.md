# LDAP Server Setup

This is part of the [User Guide for `kafka-ldap-integration`](index.md).

## Set up a Directory Server

_If you already have a directory server available for testing, you can skip this section._

* Download and install the latest stable version from [ApacheDS](https://directory.apache.org/apacheds/). It's best to use the installer for your specific operating system.
* Start the server. On Linux, the command will be something like "`sudo /etc/init.d/apacheds-<version> start`"

A fresh install of ApacheDS will have a default administration DN of "`uid=admin,ou=system`", with a bind password of "`secret`". 

## Set up Test Data

In your favourite editor, create a new file called `test.ldif`, and copy in the following data:

```ldif
dn: dc=security,dc=example,dc=com
objectClass: top
objectClass: domain
dc: security

dn: ou=users,dc=security,dc=example,dc=com
objectClass: top
objectClass: organizationalUnit
ou: users

dn: uid=srvkafkabroker,ou=users,dc=security,dc=example,dc=com
objectClass: top
objectClass: inetOrgPerson
objectClass: person
objectClass: organizationalPerson
cn: Kafka Broker
sn: Broker
uid: srvkafkabroker
userPassword: broker

dn: uid=srvbinder,ou=users,dc=security,dc=example,dc=com
objectClass: top
objectClass: inetOrgPerson
objectClass: person
objectClass: organizationalPerson
cn: Service Binder
sn: Binder
uid: srvbinder
userPassword: binder

dn: uid=srvkafkasregistry,ou=users,dc=security,dc=example,dc=com
objectClass: top
objectClass: inetOrgPerson
objectClass: person
objectClass: organizationalPerson
cn: Kafka SRegistry
sn: SRegistry
uid: srvkafkasregistry
userPassword: sregistry

dn: uid=srvkafkaproducer,ou=users,dc=security,dc=example,dc=com
objectClass: top
objectClass: inetOrgPerson
objectClass: person
objectClass: organizationalPerson
cn: Kafka Producer
sn: Producer
uid: srvkafkaproducer
userPassword: producer

dn: uid=srvkafkaproducer2,ou=users,dc=security,dc=example,dc=com
objectClass: top
objectClass: inetOrgPerson
objectClass: person
objectClass: organizationalPerson
cn: Kafka Producer2
sn: Producer2
uid: srvkafkaproducer2
userPassword: producer2

dn: uid=srvkafkaproducer3,ou=users,dc=security,dc=example,dc=com
objectClass: top
objectClass: inetOrgPerson
objectClass: person
objectClass: organizationalPerson
cn: Kafka Producer3
sn: Producer3
uid: srvkafkaproducer3
userPassword: producer3

dn: uid=srvkafkaconsumer,ou=users,dc=security,dc=example,dc=com
objectClass: top
objectClass: inetOrgPerson
objectClass: person
objectClass: organizationalPerson
cn: Kafka Consumer
sn: Consumer
uid: srvkafkaconsumer
userPassword: consumer

dn: uid=srvkafkaconsumer2,ou=users,dc=security,dc=example,dc=com
objectClass: top
objectClass: inetOrgPerson
objectClass: person
objectClass: organizationalPerson
cn: Kafka Consumer2
sn: Consumer2
uid: srvkafkaconsumer2
userPassword: consumer2

dn: uid=srvkafkaconsumer3,ou=users,dc=security,dc=example,dc=com
objectClass: top
objectClass: inetOrgPerson
objectClass: person
objectClass: organizationalPerson
cn: Kafka Consumer3
sn: Consumer3
uid: srvkafkaconsumer3
userPassword: consumer3

dn: ou=groups,dc=security,dc=example,dc=com
objectClass: top
objectClass: organizationalUnit
ou: groups

dn: cn=ktconsTest,ou=groups,dc=security,dc=example,dc=com
objectClass: groupOfUniqueNames
objectClass: top
cn: ktacons
cn: ktconstest
uniqueMember: uid=srvkafkaconsumer,ou=users,dc=security,dc=example,dc=com
uniqueMember: uid=srvkafkaconsumer2,ou=users,dc=security,dc=example,dc=com

dn: cn=ktprodTest,ou=groups,dc=security,dc=example,dc=com
objectClass: groupOfUniqueNames
objectClass: top
cn: ktaprod
cn: ktprodtest
uniqueMember: uid=srvkafkaproducer3,ou=users,dc=security,dc=example,dc=com
```

Save the file.

## Import Test Data

You can use any command line tools or GUI specific to your directory server. These instructions assume you are running the [Apache Directory Studio](https://directory.apache.org/studio/) GUI, and connecting to the ApacheDS server described in the previous section.

**Connect to the Directory Server**:
* From the `LDAP` top level menu, select `New Connection...`
* On the Network Parameter tab:
  * Hostname: `localhost`
  * Port: `10636`
  * Encryption method: `Use SSL encryption (ldaps://)`
  * Click "Next"
* On the Authentication tab:
  * Authentication method: `Simple Authentication`
  * Bind DN or user: `uid=admin,ou=system`
  * Bind password: `secret`
  * Click "Finish"
* Open the connection  

**Import the test data**:
* From the `File` top level menu, select `Import...`
* Select `LDIF into LDAP`, and click 'Next'
* Select the LDIF file you saved in the previous section, and click 'Next'
* You should now be able to browse the entries in the LDAP Browser window.

## Next Steps

 [Test Drive Kafka LDAP Integration](test.md).
