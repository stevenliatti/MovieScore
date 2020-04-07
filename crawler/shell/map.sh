#!/usr/bin/env bash

if [[ $# != 3 ]]; then
    echo "Usage: $0 <ips_file> <api_key> <input_file>"
    echo "Example : $0 ips.txt asdf movies.json"
    exit 1
fi

line=$(grep -nr $(hostname -I | cut -d' ' -f1) $1 | cut -d':' -f1)
let "machine_id = $line - 1"
machines=$(wc -l $1 | cut -d' ' -f1)
./crawler $2 $3 /tmp/movies$machine_id.json $machines $machine_id
