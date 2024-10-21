# Introduction

Mica requires [mTLS](https://en.wikipedia.org/wiki/Mutual_authentication) for all connections to and from the Mica Network. This 
means that for connections made from Mica member applications to the Mica network, the client attempting the connection 
is authenticated by the server in addition to the client authenticating the server. In addition, this requires member services
to validate the certificates presented by Mica clients. This provides an extra level of security that helps ensure only 
validate Mica members can connect to the network.

This directory contains scripts that will Mica members may use to create keys, CSRs and certificates. Most of the 
scripts call a Mica admin API and require admin certificates provided by Mica. Also, every script that calls a Mica 
service will copy the response payload in JSON format to a file.

**Prerequisites**

In order to use the scripts provided by Mica the following must be true:
1. The member has obtained admin certificates authorizing them to call administration services on Mica servers
2. The gPRC command line tool [evans](https://github.com/ktr0731/evans) has been installed and can be found on the PATH.
3. The standard security package [openssl](https://www.openssl.org/) has been installed and the `openssl` executable can be found on the PATH (optional)

## Generate the Client Certificates for Mica Calls to Member Callback Services
In our documentation we term these certificates `callback certificates` because these are the certs that secure
the calls from Mica to members services which currently only exist on the Service Provider side of Mica's
Payments network.

**Steps To Generate, Sign and Register Callback Certificates**

1. Call Mica to create a private key and Certificate Signing Request
2. Sign the Certificate using your Member Root CA
3. Update Mica with the signed Certificate and Root CA certs
4. Test the new certificates by calling the Mica admin service

### 1. Call Mica to Create a Private Key and CSR
The first step is to call Mica to create a private key and certificate signing request.

Script: `gen_from_mica_mtls_certs.sh`

Inputs:
1. Certificate display name

Outputs:
1. Mica Certificate Signing Request PEM file (`callback_<name>_<partition>.members.mica.io.csr`)

Mica will save its private key and return the Certificate Signing Request needed to create the certificate. The response 
will also contain an identifier that you will use to refer to the certificate in subsequent calls.

```text
> ./gen_from_mica_mtls_certs.sh -p ril69i -n acme_inc_cb -a /certs/mica-admin

Call to Mica to generate a callback certificate succeeded!
```
### 2. Sign the CSR Using Your Certificate Authority
We don't have a script for this because there are so many different ways members can do the certificate signing. After
signing you should have valid private key, certificate and Root CA PEM files.

### 3. Update Mica Service Provider with the Signed Certificates
Next call Mica with the results of signing the CSR: the Mica client certificate and your Root CA certificate.
Mica will store these certificates and use them in all future callback requests to your service provider callback.

Script: `update_from_mica_mtls_certs.sh`

Inputs:
1. Mica Client certificate PEM file (`callback_<name>_<partition>.members.mica.io.crt`)
2. Root CA PEM file (`callback_<name>_<partition>.members.mica.io_rootca.crt`)
3. Mica Partition ID
4. Certificate name
5. Mica Unique Certificate Identifier 

Outputs:
None

**Caution**: If there is a problem with the client certs that you send in this step it could break the connection
between the Mica Service Provider and your Callback application. Because of this we highly recommend setting `enable`
to `false` so that this new certificate will not be enabled by default. After you have successfully validated the new 
certificate in Step 4 below, you can do Step 5 to enable the certificate.

Sample Invocation:
```text
> ./update_from_mica_mtls_certs.sh -p ril69i -n test -a /certs/mica-admin -k 82fdb917-eaa9-46f7-87c4-35133a165828 \
    -c ./callback_test_ril69i.members.mica.io.crt -r callback_test_ril69i.members.mica.io_rootca.crt -e false
    
Call to Mica to update the callback certificate succeeded!
```

### 4. Test the New Certificate
In this step you run a script that will call a Mica admin service method `PingExternalWithCertificate`, that will then
make a reverse call to your Callback Service `Ping` method , using the newly minted certificates. The results of this 
reverse call will be returned to you. If the response status returned to you is `STATUS_SUCCESS`, then the certificates 
are working and can be enabled if they haven't already been enabled in the previous step.

Script: `test_from_mica_mtls_certs.sh`

Inputs:
1. Mica Certificate Identifier 

Outputs:
None

**Note** Your Callback Service for the Mica Service Provider must be restarted using the new certificates and be accessible 
via the public internet for this call to succeed. In addition your Callback Service must implement the `Ping` method as
documented [here](https://developer.mica.io/issuer/tl-dr/#implementing-a-service-provider-service)

Sample Invocation:
```text
> ./test_from_mica_mtls_certs.sh -p ril69i -a /certs/mica/admin -k 82fdb917-eaa9-46f7-87c4-35133a165828 \
    -c ./callback_test_ril69i.members.mica.io.crt -r ./callback_test_ril69i.members.io_rootca.crt 

Connection from Mica service was successful!
```
### 4. Update the Mica Service Provider to Enable the new Certificates
When you are ready to stop using the existing certificate and begin using your new certificate, you must call
Mica again to Enable the certificate you have uploaded, making it the default client certificate used for calls
from Mica to your Callback Service.

Script: `update_from_mica_mtls_certs.sh`

Inputs:
1. Mica Client certificate PEM file (`callback_<name>_<partition>.members.mica.io.crt`)
2. Root CA PEM file (`callback_<name>_<partition>.members.mica.io_rootca.crt`)
3. Mica Partition ID
4. Certificate name
5. Mica Unique Certificate Identifier
6. Enable flag set to true

Outputs:
None

Sample Invocation:
```text
> ./update_from_mica_mtls_certs.sh -p ril69i -n test -a /certs/mica-admin -k 82fdb917-eaa9-46f7-87c4-35133a165828 \
    -c ./callback_test_ril69i.members.mica.io.crt -r callback_test_ril69i.members.mica.io_rootca.crt -e true
    
Call to Mica to update the callback certificate succeeded!
```

