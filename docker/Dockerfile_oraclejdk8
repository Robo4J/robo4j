FROM anapsix/alpine-java:8_jdk as builder
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY build.gradle settings.gradle libraries.gradle gradlew  $APP_HOME
COPY . .
RUN ./gradlew robo4j-core:build