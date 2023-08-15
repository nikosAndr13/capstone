FROM maven:3.8.4-openjdk-17-slim AS BUILD

WORKDIR /

COPY ./ ./

RUN mvn clean package

FROM openjdk:8-jre-alpine3.9

COPY --from=BUILD target/Capstone-1.0-SNAPSHOT.jar /Capstone-1.0-SNAPSHOT.jar

EXPOSE 8080

CMD ["java", "-jar", "Capstone-1.0-SNAPSHOT.jar"]