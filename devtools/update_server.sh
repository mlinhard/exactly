# This is a script used during development
# It replaces files in current installation of exactly
# /opt/exactly/lib/java/exactly-server.jar

check_rpm_installed=`rpm -q exactly &> /dev/null && echo success || echo fail`

if [ ! "${check_rpm_installed}" == "success" ]; then
	print "Exactly must be installed in your system to update it"
	exit 1
fi

if [ ! -f devtools/update_server.sh ]; then
	print "Must be run from codebase root"
	exit 1
fi

if [ ! -f server/target/classes/VERSION ]; then
	print "Server must be built to produce VERSION file"
	exit 1
fi

version=`cat server/target/classes/VERSION`
sudo cp server/target/exactly-server-${version}.jar /opt/exactly/lib/java/exactly-server.jar
