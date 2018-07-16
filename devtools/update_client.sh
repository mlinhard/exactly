# This is a script used during development
# It replaces files in current installation of exactly
# /usr/bin/exactly
# /opt/exactly/lib/python
# /usr/local/lib/python3.6/site-packages/exactly-0.1.0_SNAPSHOT-py3.6.egg (using setup.py)

check_rpm_installed=`rpm -q exactly &> /dev/null && echo success || echo fail`

if [ ! "${check_rpm_installed}" == "success" ]; then
	echo "Exactly must be installed in your system to update it"
	exit 1
fi

if [ ! -f devtools/update_client.sh ]; then
	echo "Must be run from codebase root"
	exit 1
fi

if [ ! -f server/target/classes/VERSION ]; then
	echo "Server must be built to produce VERSION file"
	exit 1
fi

sudo cp server/target/classes/VERSION /opt/exactly/lib/python/VERSION
sudo cp client/bin/exactly /usr/bin/exactly

cd client
sudo python3 setup.py install

sudo rm -rf build dist exactly.egg-info