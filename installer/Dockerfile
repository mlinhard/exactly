FROM fedora:30

RUN yum -y update && yum clean all

RUN mkdir -p /go && chmod -R 777 /go && \
  yum -y install \
    git \
    golang \
    rpm-build \
    python3 \
    python3-setuptools \
  && yum clean all
