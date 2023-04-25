FROM openjdk:17-jdk-slim

RUN mkdir /app
WORKDIR /app

#COPY ./build/libs/ups-0.0.1-SNAPSHOT.jar .

ENTRYPOINT ["sh", "-c", "sleep 3 && java -jar ups-0.0.1-SNAPSHOT.jar"]
