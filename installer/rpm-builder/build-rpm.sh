#!/bin/bash
cd /home/installer/server
mvn clean install

cd /home/installer
mkdir -p BUILD/usr/bin
mkdir -p BUILD/opt/exactly/lib/java
mkdir -p BUILD/opt/exactly/lib/python

jar xf server/target/exactly-server-*.jar BOOT-INF/classes/VERSION
mv BOOT-INF/classes/VERSION client/VERSION
rm -rf BOOT-INF

cd /home/installer/client

version=`cat VERSION`
rpm_version=`cat VERSION | sed 's/\(.*\)-SNAPSHOT/\1/g'`
rpm_release=1

cd /home/installer
mv server/target/exactly-server-$version.jar BUILD/opt/exactly/lib/java/exactly-server.jar
mv client/bin/exactly BUILD/usr/bin
rm -rf client/bin
cp -r client/* BUILD/opt/exactly/lib/python

rpmbuild -bb --buildroot /home/installer/BUILD \
  --define "version $rpm_version" \
  --define "release $rpm_release" \
  --define "_rpmdir /home/installer/rpm" \
  package.spec

host_uid=$1
host_gid=$2

chown -R $host_uid:$host_gid rpm
