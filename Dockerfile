FROM ubuntu:22.04

WORKDIR /app

COPY . /app

RUN sed -i 's@archive.ubuntu.com@mirror.kakao.com@g' /etc/apt/sources.list &&  \
    apt-get update && \
    apt-get install -y wget gcc g++ make python3 && \
    wget https://bootstrap.pypa.io/get-pip.py && python3 get-pip.py && rm get-pip.py && \
    wget https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz && \
    tar xf openjdk-17.0.2_linux-x64_bin.tar.gz && \
    rm openjdk-17.0.2_linux-x64_bin.tar.gz && \
    mv jdk-17.0.2 /java

ENV JAVA_HOME /java

RUN export JAVA_HOME && \
    cd UVNCRepeater && make && cd .. && \
    cd websockify && python3 setup.py install && cd .. && \
    export SPRING_PROFILES_ACTIVE=local

#CMD ["./gradlew", "bootRun"]
#CMD ["./UVNCRepeater/repeater", "uvncrepeater.ini"]
#CMD ["websockify", "6080", "127.0.0.1:5900"]

EXPOSE 8443
EXPOSE 6080
EXPOSE 5500