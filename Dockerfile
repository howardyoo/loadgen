# base image
FROM openjdk:8-jdk-alpine

LABEL maintainer="hgy@gmail.com"
VOLUME /tmp
EXPOSE 8080

RUN apk update
RUN apk add curl

ARG JAR_FILE=target/loadgen-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} loadgen-0.0.1.jar

# run the jar file
ENTRYPOINT ["java","-Djava.security.edg=file:/dev/./urandom","-jar","/loadgen-0.0.1.jar"]

