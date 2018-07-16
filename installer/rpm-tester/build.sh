#!/bin/bash

if [ ! `basename $PWD` == rpm-tester -o ! -f Dockerfile ]; then
	echo "You must run this in installer/rpm-tester folder"
	exit 1
fi

arch=x86_64

rm -rf rpm
cp -r ../rpm-builder/rpm .

pushd rpm/${arch}

if [ `ls -1 | wc -l` != 1 ]; then
	echo "There are multiple RPMs, please clean the old RPMs before proceeding"
	exit 1
fi 

rpm_version=`rpm -qp --queryformat="%{version}" exactly-*.${arch}.rpm`
rpm_release=`rpm -qp --queryformat="%{release}" exactly-*.${arch}.rpm`

popd 

rpm_name=exactly-${rpm_version}-${rpm_release}.x86_64.rpm

cp ../rpm-builder/rpm/x86_64/$rpm_name .

docker build --build-arg "rpm_name=${rpm_name}" -t exactly-rpm-tester . 

docker run -i -t exactly-rpm-tester ./test.sh

