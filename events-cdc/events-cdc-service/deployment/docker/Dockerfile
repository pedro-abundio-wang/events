# Note: must specify the following options
# 1) build context
# 2) build arg

FROM openjdk:8-jdk-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# https://stackoverflow.com/questions/60318098/pass-args-in-docker-entrypoint
ARG buildVersion
ENV buildVersion=$buildVersion

EXPOSE 8080

COPY ./build/libs/events-cdc-service-$buildVersion.jar events-cdc-service-$buildVersion.jar

HEALTHCHECK --start-period=5s --interval=60s --timeout=3s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT java -jar /events-cdc-service-$buildVersion.jar
