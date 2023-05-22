FROM --platform=linux/amd64 eclipse-temurin:18-jre
COPY inventory-0.0.1-SNAPSHOT.jar server.jar
CMD ["java","-jar","server.jar"]