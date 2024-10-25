#! /bin/bash

help() {
    echo "Usage: $0 -p <partition> -n <display_name> -a <path to admin certs dir> -c <cert file> -r <rootca file> -k <cert ref> -e <enabled>"
    echo "This script calls mica to enable a \"from mica\" external client certificate, making it the default."
    echo "Options:"
    echo "    -p <partition>         Mica partition id (Required)"
    echo "    -n <display-name>      Certificate display name (Required, can be up to 10 characters in length)"
    echo "    -a <admin-cert-path>   path to the folder containing the admin rootca, crt and key files (Required)"
    echo "    -k <cert-ref-id>       Unique identifier for this certificate in the Mica system (Required)"
    echo "    -h this help menu"
    echo "Note that the file names for the admin certificate/key files should conform to the following:"
    echo "rootca: admin_\${partition}.members.mica.io_rootca.crt"
    echo "cert:   admin_\${partition}.members.mica.io.crt"
    echo "key:    admin_\${partition}.members.mica.io.key"
    echo ""
    echo "The Mica service hostname and port default to \"api.\${partition}.members.mica.io\" and 443, respectively."
    echo "You may override these defaults by setting the env vars: MICA_HOST and MICA_PORT"
    echo ""
    echo "Please note that you must install the gRPC tool \"evans\" to run this script."
    echo "  see the Evans website for details, https://github.com/ktr0731/evans?tab=readme-ov-file "
}
############################################################################################
# Please see the script gen_to_mica_csr.sh to generate the CSR file required by this script
############################################################################################

partition=""
adminpath=""
certref=""

while getopts p:n:a:c:r:k:e:h flag
do
    case "${flag}" in
        p) partition=${OPTARG};;
        a) adminpath=${OPTARG};;
        k) certref=${OPTARG};;
        h) help && exit 0;;
       \?) # Invalid option
          echo "Error: Invalid option"
          help && exit 0;;
    esac
done
evansloc=$(which evans)

if [[ -z ${evansloc} ]] ; then
    echo "ERROR: evans not installed or not found on the current user's PATH"
    help
    exit 1
fi

if [[ -z ${partition} ]] ; then
    echo "ERROR: a partition name must be defined"
    help
    exit 1
fi

if [[ -z ${certref} ]] ; then
    echo "ERROR: a certificate ref must be defined"
    help
    exit 1
fi

if [[ -z ${adminpath} ]] ; then
    echo "ERROR: the path to the admin certs must be defined"
    help
    exit 1
fi

if [[ ! -d "${adminpath}" ]]; then
  echo "ERROR: the admin certs directory \"${adminpath}\" does not exist or is not a directory"
  exit 1
fi

admin_rootca_file="${adminpath}/admin_${partition}.members.mica.io_rootca.crt"
admin_cert_file="${adminpath}/admin_${partition}.members.mica.io.crt"
admin_key_file="${adminpath}/admin_${partition}.members.mica.io.key"

if [[ ! -f "$admin_rootca_file" ]]; then
  echo "Admin rootca file ${admin_rootca_file} does not exist"
  exit 1
fi

if [[ ! -f "$admin_cert_file" ]]; then
  echo "Admin cert file ${admin_cert_file} does not exist"
  exit 1
fi

if [[ ! -f "$admin_key_file" ]]; then
  echo "Admin key file ${admin_key_file} does not exist"
  exit 1
fi


default_host="api.${partition}.members.mica.io"
if [[ -z "$MICA_HOST" ]]; then
#  echo "defaulting to $default_host"
  MICA_HOST="${default_host}"
fi

if [[ -z "$MICA_PORT" ]]; then
#  echo "defaulting to port 443"
  MICA_PORT=443
fi

OUT=/tmp/$$.out

#echo "calling $service"
jq --null-input  --arg certrefkey "${certref}"  '{
  "certificate_ref_key": $certrefkey
}' | evans  \
    cli call mica.serviceprovider.administration.v1.ServiceProviderAdministrationService.EnableFromMicaCertificate \
    --host $MICA_HOST --port $MICA_PORT --reflection --tls \
    --cacert $admin_rootca_file \
    --cert $admin_cert_file \
    --certkey $admin_key_file > $OUT

RC=$?
if [[ "$RC" -ne 0 ]]; then
  echo "Evans call failed"
  exit 1
fi
cp $OUT "enable_callback_cert_response.json"

mica_status=$(jq -r .status < $OUT)

if [[ "${mica_status}" != "STATUS_SUCCESS" ]]; then
  echo "the call to mica to enable the certificate did not succeed. status was ${status}"
  exit 1
fi

cp $OUT "update_callback_cert_response.json"

echo "Call to Mica to enable the callback certificate succeeded!"