FROM ubuntu:20.04

WORKDIR /app
COPY . /app

RUN sed -i 's@archive.ubuntu.com@mirror.kakao.com@g' /etc/apt/sources.list && apt update && \
    apt install -y software-properties-common && \
    add-apt-repository ppa:deadsnakes/ppa && apt update && \
    apt install -y openjdk-17-jdk gcc g++ make python3.10 python3.10-distutils curl && \
    curl -sS https://bootstrap.pypa.io/get-pip.py && \
    export SPRING_PROFILES_ACTIVE=local

WORKDIR /app/UVNCRepeater
RUN make .

WORKDIR /app/websockify
RUN pip install setuptools && python3.10 setup.py install

WORKDIR /app

CMD ["./gradlew", "bootRun"]
CMD ["./UVNCRepeater/repeater", "uvncrepeater.ini"]
CMD ["websockify", "6080", "127.0.0.1:5900"]

EXPOSE 8080