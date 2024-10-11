FROM openjdk:21
LABEL org.opencontainers.image.authors="qubic.org"
COPY target/qx-service-0.1.0-SNAPSHOT.jar qx-service.jar
ENTRYPOINT ["java","-jar","/qx-service.jar", "--sync.enabled=true"]