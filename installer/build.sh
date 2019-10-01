#!/bin/sh

if [ $# == 0 ]; then
    # on host machine
    rm -rf tmp
    mkdir tmp
    cp -r ../server tmp
    cp -r ../client tmp
    cp ./build.sh tmp
    docker build . -t exactly-server-builder
    docker run -i -t -w /build -v "$PWD/tmp:/build:rw,z" exactly-server-builder /build/build.sh `id -u` `id -g` `git describe --tags`
    mv tmp/*.rpm .
    rm -rf tmp
else
    # inside docker
    host_uid=$1
    host_gid=$2
    version=$3
    pushd server
    go build -o exactly-index "-ldflags=-s -w -X main.Version=${version}"
    popd
    mv server/exactly-index client/bin
    export EXACTLY_VERSION=${version}
    pushd client
    python3 setup.py bdist_rpm --binary-only --force-arch x86_64 --prep-script rpm-prep
    popd
    mv client/dist/*.rpm .
    chown -R $host_uid:$host_gid *
fi



