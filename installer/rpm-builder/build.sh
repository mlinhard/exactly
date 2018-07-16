if [ ! `basename $PWD` == rpm-builder -o ! -f Dockerfile ]; then
	echo "You must run this in installer/rpm-builder folder"
	exit 1
fi

rm -rf sources
mkdir sources
cp -r ../../client sources/client
cp -r ../../server sources/server
rm -rf sources/client/*.pyc

if [ ! -d cache/maven-repoload ]; then
	mkdir -p cache/maven-repoload
	cp -r ../../server cache/maven-repoload
fi

docker build -t exactly-rpm-builder-maven-repoload -f Dockerfile.maven-repoload .
docker build -t exactly-rpm-builder .
rm -rf rpm
mkdir rpm
docker run -i -t -v "$PWD/rpm:/home/installer/rpm:rw,z" exactly-rpm-builder ./build-rpm.sh `id -u` `id -g`

