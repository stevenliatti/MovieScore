#!/usr/bin/env bash

source .env

line=$(grep -nr $(hostname -I | cut -d' ' -f1) $IPS | cut -d':' -f1)
let "machine_id = $line - 1"
echo "REDUCE: send movies$machine_id.json"
./cloudsend.sh movies$machine_id.json $NEXTCLOUD_UPLOAD
