FROM maven:3.8.4-openjdk-20

WORKDIR /app

COPY . .

RUN ["mvn", "install"]

ENTRYPOINT ["java", "-jar", "./target/noticeme-0.0.1-SNAPSHOT.jar"]