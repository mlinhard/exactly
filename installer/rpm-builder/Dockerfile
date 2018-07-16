FROM exactly-rpm-builder-maven-repoload

WORKDIR /home/installer

# refresh sources
RUN rm -rf maven-repoload-server maven-repoload.sh
COPY sources/client /home/installer/client
COPY sources/server /home/installer/server
COPY build-rpm.sh /home/installer/build-rpm.sh
COPY package.spec /home/installer/package.spec
