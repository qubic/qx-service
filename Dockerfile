FROM openjdk:21
MAINTAINER qubic.org
COPY target/qx-service-0.1.0-SNAPSHOT.jar qx-service.jar
ENTRYPOINT ["java","-jar","/qx-service.jar"]