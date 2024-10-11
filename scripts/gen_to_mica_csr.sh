#!/bin/bash

help() {
    echo "Usage: $0 -p <partition> -n <name> -o <organization> -u <unit> -l <locality> -s <state>"
    echo "This script generates a csr and private key for \"to mica\" certificates"
    echo "Options:"
    echo "    -p <partition> Required"
    echo "    -n <name> the name for the certificate, makes it to the CN and has to be 10 characters or less. Default testcert0"
    echo "    -o <organization> the organization part of the CN. Default mica"
    echo "    -u <unit> the organization unit part of the CN. Default engineering"
    echo "    -s <state> the state part of the CN. Default is TX"
    echo "    -l <locality> the locality part of the CN. Default is Austin"
    echo "    -h this help menu"
}

partition=""
organization="mica"
unit="engineering"
state="TX"
locality="Austin"
certname="testcert0"

while getopts p:n:o:s:l:u:h flag
do
    case "${flag}" in
        p) partition=${OPTARG};;
        n) certname=${OPTARG};;
        o) organization=${OPTARG};;
        u) unit=${OPTARG};;
        s) state=${OPTARG};;
        l) locality=${OPTARG};;
        h) help && exit 0;;
    esac
done

if [[ -z ${partition} ]] ; then
    echo "A partition name must be defined"
    help
    exit 1
fi

if [[ ${#certname} -gt 10 ]] ; then
    echo "The certificate name needs to be less than 10 characters"
    help
    exit 1
fi

echo Partition ${partition}
echo Organization ${organization}
echo Unit ${unit}
echo State ${state}
echo Locality ${locality}
echo certname ${certname}

output="externalclient_${certname}_${partition}.members.mica.io"
outputcsr=${output}.csr
outputkey=${output}.key
subject="/C=US/ST=${state}/L=${locality}/O=${organization}/OU=${unit}/CN=externalclient-${certname}.${partition}.members.mica.io"

echo Subject is: ${subject}
openssl req -new -newkey rsa:2048 -nodes -out ${outputcsr} -keyout ${outputkey} -subj ${subject}
