#/bin/bash

# this script generates jwt keypair
openssl genrsa -out private.key 4096
openssl rsa -in private.key -pubout -out public.key
openssl pkcs8 -topk8 -nocrypt -in private.key -out private.pkcs8.key