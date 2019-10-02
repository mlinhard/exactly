#!/bin/sh

if [ $# == 0 ]; then
    # on host machine
    rm -rf tmp
    mkdir tmp
    cp *.rpm tmp
    cp test.sh tmp
    docker run -i -t -w /build -v "$PWD/tmp:/build:rw,z" fedora:30 /build/test.sh `id -u` `id -g` `git describe --tags`
    rm -rf tmp
else
    # inside docker
    host_uid=$1
    host_gid=$2
    version=$3
    dnf install -y *.rpm
    mkdir -p /root/.config/exactly
    echo '{
    "roots" : ["/root/.config/exactly"],
    "listen_address": "localhost:9201"
}' > /root/.config/exactly/server-config.json
    exactly-index &
    sleep 0.2
    if [ `curl http://localhost:9201/version` == $version ]; then
        echo "TEST PASSED"
    else
        echo "TEST FAILED"
    fi
fi



