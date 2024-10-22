# Introduction

Mica requires [mTLS](https://en.wikipedia.org/wiki/Mutual_authentication) for all connections to and from the Mica Network. This 
means that for connections made from Mica member applications to the Mica network, the client attempting the connection 
is authenticated by the server in addition to the client authenticating the server. In addition, this requires member services
to validate the certificates presented by Mica clients. This provides an extra level of security that helps ensure only 
validate Mica members can connect to the network.

This directory contains scripts that Mica members can use to create keys, CSRs and certificates for connections made
from your services to Mica. Most of the scripts call a Mica admin API and require admin certificates provided by Mica. 
Also, every script that calls a Mica service will copy the response payload in JSON format to a file.

**Prerequisites**

In order to use the scripts provided by Mica the following must be true:
1. The member has obtained admin certificates authorizing them to call administration services on Mica servers
2. The gPRC command line tool [evans](https://github.com/ktr0731/evans) has been installed and can be found on the PATH.
3. The standard security package [openssl](https://www.openssl.org/) has been installed and the `openssl` executable can be found on the PATH (optional)

## Generate Client Certificates to Call Mica Services
There are three basic steps required to generate the keys and certificates required to call Mica Partner or
Service Provider APIs.

1. Generate the Private Key and Certificate Signing Request using `openssl`
2. Call the Mica API to generate the signed certificate
3. Call the Mica API Ping method to test the new certificate

### 1. Generate the CSR and Private Key Files
We have provided a script that calls the `openssl` executable to generate a Certificate Signing Request (CSR) and private
key file. Run this script to generate the files needed in subsequent steps.

Script: `gen_to_mica_csr.sh`

Inputs:
1. Company name
2. Company unit
3. Company state
4. Company locality (city)
5. Mica Partition ID (provided by Mica)
6. Certificate Display Name
7. Path to the location of Mica Admin API certificates

Outputs:
1. Certificate signing request PEM file (`externalclient_<name>_<partition>.members.mica.io.csr`)
2. Private key PEM file (`externalclient_<name>_<partition>.members.mica.io.key`)
   
The first file will be sent to Mica to create a signed certificate. The second is a private key file needed to test the connection
to Mica and to use for subsequent connections for your applications. You should save this file in a secure location.

Sample Invocation:
```text
>  ./gen_to_mica_csr.sh -p hron3n -n test -o Acme -u Engineering -s CA -l "San Jose" 
Partition:    hron3n
Organization: Acme
Unit:         Engineering
State:        CA
Locality:     San Jose
Cert Name:    test
Subject is: /C=US/ST=CA/L=San Jose/O=Acme/OU=Engineering/CN=externalclient-test.hron3n.members.mica.io
....+...+..+.........+.+...+...+..............+.+..+.+..+....+.........+..+....+........+...+...+....+...+++++++++++++++++++++++++++++++++++++++*.......+++++++++++++++++++++++++++++++++++++++*.+............+...+....+.....+...+...............+............+.+........+.......+...+...............+.........+.....+................+..+...+......+...+.......+.....+....+..+....+.....+..........+...+......+..+............+...+............+...+...+.+........+......+......+.........+.+...........+...+.........+.......+........................+....................+.++++++
.....+..+.......+........+.+..+++++++++++++++++++++++++++++++++++++++*.....+....+..+....+...+......+.........+...+........+....+......+.....+......+...+.+++++++++++++++++++++++++++++++++++++++*.....+....+..+.......+..+...+............+....+............+.....+............++++++
-----
```
Note that there are many other ways of generating private keys and CSRs. This script is provided for convenience and its
use is entirely optional. If you generate the key using some other tool, note that it should be of type `rsa:2048` or larger.

### 2. Call Mica to Create the Signed Certificate
The script use in this step will call Mica to upload the CSR you created in Step 1. Mica will sign the CSR and return 
the signed certificate and Root CA PEM files in the response.

Script: `gen_to_mica_mtls_cert.sh`

Inputs:
1. Certificate signing request PEM file (`externalclient_<name>_<partition>.members.mica.io.csr`)
2. Private key PEM file (`externalclient_<name>_<partition>.members.mica.io.key`)
3. Mica partition ID - provided by Mica
4. Certificate display name

Outputs:
1. Client certificate PEM file (`externalclient_test_hron3n.members.mica.io.crt`)
2. Root CA PEM file (`externalclient_test_hron3n.members.mica.io_rootca.crt`)

These two files, along with the private key generated in Step 1 will be required to make calls to Mica and should
be stored in a secure, known location.

Sample Invocation:
```text
> ./gen_to_mica_mtls_certs.sh -p hron3n -n test \
                            -a ./hron3n-admin-certs \
                            -c ./externalclient_test_hron3n.members.mica.io.csr \
                            -m partner
                            
Call to Mica generate to client certificate succeeded!
```

### 3. Call Mica to Test the Signed Certificate
After generating the certificates, you need to test them using the Mica API service.

Script: `test_to_mica_mtls_certs.sh`

Inputs:
1. Client certificate PEM file (`externalclient_<name>_<partition>.members.mica.io.crt`)
2. Client key PEM file (`externalclient_<name>_<partition>.members.mica.io.key`) 
3. Mica Root CA PEM file (`externalclient_<name>_<partition>.members.mica.io_rootca.crt`)

Outputs:
None

Sample Invocation:
```text
> ./test_to_mica_mtls_certs.sh -p hron3n -n test -m partner 

Connection to Mica service was successful!
```
This script assumes that all the files generated in the previous steps are in the current directory. If the certificate 
files are not in the current directory you must supply the path to the directory containing the files 
using an additional `-c` command line parameter:
```text
> ./test_to_mica_mtls_certs.sh -p hron3n -n test -m partner -c /certs/mica-client
```

At this point your newly created certificates can be used from your member callback application.