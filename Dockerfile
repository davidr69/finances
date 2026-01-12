#ARG APP_VERSION="3.9.1"
#
#FROM registry:5000/openjdk:21-ea-27-slim
#
#COPY build/libs/finances-${APP_VERSION}.jar /app/
#WORKDIR /app
#USER nobody
#
#ENTRYPOINT ["/bin/sh", "-c", "java -Dspring.profiles.active=${profile} -Dspring.config.additional-location=${additional_properties} -jar build/libs/finances-3.9.1.jar"]

# Build stage
ARG APP_VERSION="3.9.7"

FROM docker.io/gradle:9-jdk25 AS builder
ARG APP_VERSION
WORKDIR /build
COPY . .
RUN gradle build -x test

# Runtime stage
FROM registry:5000/awscorretto:25
ARG APP_VERSION="3.9.7"
COPY --from=builder /build/build/libs/finances-${APP_VERSION}.jar /app/finances.jar
WORKDIR /app
USER nobody
ENTRYPOINT ["/bin/sh", "-c", "java -jar finances.jar"]
