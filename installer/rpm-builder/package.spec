Name: exactly
Version: %{version}
Release: %{release}
Summary: Binary exact search
License: Apache License 2.0
Group: Applications/Productivity
Packager: Michal Linhard <michal@linhard.sk>
Requires: java-1.8.0-headless python3 python3-setuptools

%%pre
#noop

%%post
cd /opt/exactly/lib/python
cp VERSION /tmp/exactly_VERSION
python3 setup.py install

%%preun
#noop

%%postun
pip3 uninstall exactly

%%description
Tool for exact substring search in multiple text or binary files.

%%files
/usr/bin/exactly
/opt/exactly/lib/java/exactly-server.jar
/opt/exactly/lib/python/*