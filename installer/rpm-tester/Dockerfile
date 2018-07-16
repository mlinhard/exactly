FROM centos:centos7

ARG rpm_name

WORKDIR /home/installer

COPY ${rpm_name} ${rpm_name}

RUN yum install -y ${rpm_name} 

COPY test.sh test.sh
