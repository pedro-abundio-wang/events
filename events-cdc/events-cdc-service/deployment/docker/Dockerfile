FROM openjdk:8-jdk-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8080

# TODO remove version
COPY ../../build/libs/events-cdc-service-v0.0.1.jar service.jar

HEALTHCHECK --start-period=5s --interval=60s --timeout=3s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/service.jar"]
