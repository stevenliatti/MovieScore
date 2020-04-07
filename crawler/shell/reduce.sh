#!/usr/bin/env bash

if [[ $# != 2 ]]; then
    echo "Usage: $0 <ips_file> <cloud_url>"
    echo "Example : $0 ips.txt https://nextcloud.com"
    exit 1
fi

line=$(grep -nr $(hostname -I | cut -d' ' -f1) $1 | cut -d':' -f1)
let "machine_id = $line - 1"
curl -O 'https://gist.githubusercontent.com/tavinus/93bdbc051728748787dc22a58dfe58d8/raw/cloudsend.sh' && chmod +x cloudsend.sh
./cloudsend.sh /tmp/movies$machine_id.json $2
