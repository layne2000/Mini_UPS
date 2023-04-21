FROM openjdk:17-jdk-slim

RUN mkdir /app
WORKDIR /app

ARG JAR_FILE=stock_exchange_system-1.0-SNAPSHOT.jar

COPY ${JAR_FILE} stock_exchange_system-1.0-SNAPSHOT.jar

ENTRYPOINT ["sh", "-c", "sleep 3 && java -jar stock_exchange_system-1.0-SNAPSHOT.jar"]
