FROM maven:3.8.4-openjdk-17-slim AS BUILD

WORKDIR /

COPY . .

RUN mvn clean package

FROM openjdk:11-jre-slim

ENV URLS=jdbc:postgresql://trumpet.db.elephantsql.com:5432/tmwqmrtr
ENV DB_USER=tmwqmrtr
ENV DB_PASSWORD=ubL-UpR6OnoBkBKPm4oG7eqhkiix09Db

COPY --from=BUILD target/Capstone-1.0-SNAPSHOT.jar /Capstone-1.0-SNAPSHOT.jar

EXPOSE 8080

CMD ["java", "-jar", "Capstone-1.0-SNAPSHOT.jar"]