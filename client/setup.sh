#!/bin/bash
if [ "$1" != "update" ]; then
    rm -rf .venv
    python3 -m virtualenv .venv
fi

source .venv/bin/activate

export EXACTLY_VERSION=`git describe --tags`

if [ ! -f bin/exactly-index ]; then 
    if [ ! -f ../server/exactly-index ]; then
        pushd ../server
        go build -o exactly-index "-ldflags=-s -w -X main.Version=${EXACTLY_VERSION}"
        popd
    fi
    mv ../server/exactly-index bin
fi

python3 setup.py install

# for some reaason the setup in virtualenv doesn't copy bin/exactly-index to .venv/bin
find .venv -name exactly-index -exec rm {} \;
mv bin/exactly-index .venv/bin

rm -rf build dist exactly.egg-info

deactivate

