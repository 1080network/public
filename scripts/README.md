# Introduction

Mica requires [mTLS](https://en.wikipedia.org/wiki/Mutual_authentication) for all connections to and from the Mica Network. This 
means that for connections made from Mica member applications to the Mica network, the client attempting the connection 
is authenticated by the server in addition to the client authenticating the server. In addition, this requires member services
to validate the certificates presented by Mica clients. This provides an extra level of security that helps ensure only 
validate Mica members can connect to the network.

This directory contains scripts that will Mica members may use to create keys, CSRs and certificates. 

## Generate the Client Certificates to Call Mica Services
There are three basic steps required to generate the keys and certificates required to call Mica Partner or
Service Provider APIs.

1. Generate the Private Key and Certificate Signing Request using `openssl`
2. Call the Mica API to generate the signed certificate
3. (optional) Call the Mica API Ping method to test the new certificate

**Prerequisites**

In order to use the scripts provided by Mica the following must be true:
1. The member has obtained admin certificates authorizing them to call administration services on Mica servers
2. The gPRC command line tool [evans](https://github.com/ktr0731/evans) has been installed and can be found on the PATH.
3. The standard security package [openssl](https://www.openssl.org/) has been installed and the `openssl` executable can be found on the PATH

### 1. Generate the CSR and Private Key Files
We have provided a script that calls the `openssl` executable to generate a Certificate Signing Request (CSR) and private
key file. Run this script to generate the files needed in subsequent steps.

```text
>  ./gen_to_mica_csr.sh -p hron3n -n test
Partition hron3n
Organization mica
Unit engineering
State TX
Locality Austin
certname test
Subject is: /C=US/ST=TX/L=Austin/O=mica/OU=engineering/CN=externalclient-test.hron3n.members.mica.io
....+...+..+.........+.+...+...+..............+.+..+.+..+....+.........+..+....+........+...+...+....+...+++++++++++++++++++++++++++++++++++++++*.......+++++++++++++++++++++++++++++++++++++++*.+............+...+....+.....+...+...............+............+.+........+.......+...+...............+.........+.....+................+..+...+......+...+.......+.....+....+..+....+.....+..........+...+......+..+............+...+............+...+...+.+........+......+......+.........+.+...........+...+.........+.......+........................+....................+.++++++
.....+..+.......+........+.+..+++++++++++++++++++++++++++++++++++++++*.....+....+..+....+...+......+.........+...+........+....+......+.....+......+...+.+++++++++++++++++++++++++++++++++++++++*.....+....+..+.......+..+...+............+....+............+.....+............++++++
-----
```

This will produce two files:
1. `externalclient_test_hron3n.members.mica.io.csr`
2. `externalclient_test_hron3n.members.mica.io.key`

The first file is needed to send to Mica to create a signed certificate. The second file is needed to test the connection
the Mica and to use for subsequent connections for your applications.

### 2. Call Mica to Create the Signed Certificate
```text
> ./gen_to_mica_mtls_certs.sh -p hron3n -n test \
                            -a ./hron3n-admin-certs \
                            -c ./externalclient_test_hron3n.members.mica.io.csr \
                            -m partner
                            
Call to Mica generate to client certificate succeeded!
```
### 3. Call Mica to Test the Signed Certificate

```text
> ./ping_mica.sh -p hron3n -n test -m partner

Connection to Mica service was successful!
```
This script assumes that all the files generated in the previous steps are in the current directory:
```text
> ls
README.md					                    externalclient_test_hron3n.members.mica.io_rootca.crt
externalclient_test_hron3n.members.mica.io.crt  gen_to_mica_csr.sh
externalclient_test_hron3n.members.mica.io.csr  gen_to_mica_mtls_certs.sh
externalclient_test_hron3n.members.mica.io.key  ping_mica.sh
```
If the certificate files are not in the current directory you must supply the path to the directory containing the files 
using an additional `-c` command line parameter:
```text
> ./ping_mica.sh -p hron3n -n test -m partner -c /tmp/mycerts
```
## Generate the Client Certificates for Mica to Call Member Services
TBD