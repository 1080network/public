#! /bin/bash

help() {
    echo "This script calls mica to test a \"to mica\" certificates using previously generated certificate files"
    echo "Usage: $0 -p <partition> -n <name> -c <path to cert dir> -m <mica role> "
    echo "Options:"
    echo "    -p <partition>       Mica partition id (Required)"
    echo "    -n <name>            Certificate name (Required) "
    echo "    -c <cert-path>       path to the folder containing the rootca, crt and key files (default is current dir)"
    echo "    -m <mica-role>       this should be one of partner|serviceprovider (Required)"
    echo "    -h this help menu"
    echo "Note that the file names for the certificate/key files should conform to the following:"
    echo "rootca: externalclient_\${name}_\${partition}.members.mica.io_rootca.crt"
    echo "cert:   externalclient_\${name}_\${partition}.crt"
    echo "key:    externalclient_\${name}_\${partition}.key"
    echo ""
    echo "The Mica service hostname and port default to \"api.\${partition}.members.mica.io\" and 443, respectively."
    echo "You may override these defaults by setting the env vars: MICA_HOST and MICA_PORT"
    echo ""
    echo "Please note that you must install the gRPC tool \"evans\" to run this script."
    echo "  see the Evans website for details, https://github.com/ktr0731/evans?tab=readme-ov-file "
}

partition=
name=""
certpath=`pwd`
micarole=""

while getopts p:n:m:c:h flag
do
    case "${flag}" in
        p) partition=${OPTARG};;
        n) name=${OPTARG};;
        m) micarole=${OPTARG};;
        c) certpath=${OPTARG};;
        h) help && exit 0;;
       \?) # Invalid option
               echo "Error: Invalid option"
               help && exit 0;;
    esac
done

evansloc=$(which evans)

if [[ -z ${partition} ]] ; then
    echo "ERROR: a partition name must be defined"
    help
    exit 1
fi

if [[ -z ${name} ]] ; then
    echo "ERROR: a certificate name must be defined"
    help
    exit 1
fi
if [[ -z ${micarole} ]] ; then
    echo "ERROR: the mica role must be defined"
    help
    exit 1
fi


if [[ -z ${evansloc} ]] ; then
    echo "ERROR: evans not installed or not found on the current user's PATH"
    help
    exit 1
fi

rootca_file="${certpath}/externalclient_${name}_${partition}.members.mica.io_rootca.crt"
cert_file="${certpath}/externalclient_${name}_${partition}.members.mica.io.crt"
key_file="${certpath}/externalclient_${name}_${partition}.members.mica.io.key"

if [[ ! -f "$rootca_file" ]]; then
  echo "ERROR: rootca file ${rootca_file} does not exist"
  exit 1
fi

if [[ ! -f "$cert_file" ]]; then
  echo "ERROR: cert file ${cert_file} does not exist"
  exit 1
fi

if [[ ! -f "$key_file" ]]; then
  echo "ERROR: key file ${key_file} does not exist"
  exit 1
fi

default_host="api.${partition}.members.mica.io"
if [[ -z "$MICA_HOST" ]]; then
#  echo "Warning: defaulting Mica host to $default_host"
  MICA_HOST="${default_host}"
fi

if [[ -z "$MICA_PORT" ]]; then
#  echo "Waning: defaulting Mica port to 443"
  MICA_PORT=443
fi

service=""
if [[ "${micarole}" == "partner" ]]; then
  service="mica.partner.service.v1.PartnerToMicaService.Ping"
elif [[ "${micarole}" == "partner" ]]; then
  service="mica.serviceprovider.service.v1.ServiceProviderToMicaService.Ping"
else
  echo "ERROR: the mica role \"${micarole}\" is invalid, must be either \"partner\" or \"serviceprovider\" "
  exit 1
fi

OUT=/tmp/$$.out

echo "{}" | evans  cli call  ${service} \
    --host $MICA_HOST --port $MICA_PORT --reflection --tls \
    --cacert $rootca_file \
    --cert $cert_file \
    --certkey $key_file > $OUT

RC=$?
if [[ "$RC" -ne 0 ]]; then
  echo "Error: Evans call failed"
  exit 1
fi

mica_status=$(jq -r .status < $OUT)
cp $OUT "ping_response.json"

if [[ "${mica_status}" != "STATUS_SUCCESS" ]]; then
  echo "ERROR: the call to mica to test the certificates did not succeed. status was \"${mica_status}\" "
  cat ping_response.json
  exit 1
fi
echo "Connection to Mica service was successful!"


