FROM openjdk:21
MAINTAINER qubic.org
COPY target/qx-service-0.0.1-SNAPSHOT.jar qx-service.jar
ENTRYPOINT ["java","-jar","/qx-service.jar"]