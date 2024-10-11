FROM openjdk:21
LABEL org.opencontainers.image.authors="qubic.org"
COPY target/qx-sync-0.2.0-SNAPSHOT.jar qx-sync.jar
ENTRYPOINT ["java","-jar","/qx-sync.jar", "--sync.enabled=true"]