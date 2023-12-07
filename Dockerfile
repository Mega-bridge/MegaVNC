# syntax=docker/dockerfile:1

FROM amazoncorretto:17

COPY . /app

WORKDIR /app

COPY gradlew .
COPY gradle gradle

RUN chmod +x ./gradlew
RUN ./gradlew build || return 0

COPY build/libs/MegaVNC-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/app.jar"]