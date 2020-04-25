#!/usr/bin/env bash

if [[ $# != 1 ]]; then
    echo "Usage: $0 <ips_file>"
    echo "Example : $0 ips.txt"
    exit 1
fi

line=$(grep -nr $(hostname -I | cut -d' ' -f1) $1 | cut -d':' -f1)
let "machine_id = $line - 1"
wc /tmp/movies$machine_id.json
ls -lh /tmp/movies$machine_id.json
