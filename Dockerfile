#
# Build stage
#
FROM maven:3.8.2-jdk-11 AS build
COPY . .
RUN mvn clean package -Pprod -DskipTests

#
# Package stage
#
FROM openjdk:11-jdk-slim
COPY --from=build /target/cred-0.0.1-SNAPSHOT.jar cred.jar
ENTRYPOINT ["java","-jar","cred.jar"]