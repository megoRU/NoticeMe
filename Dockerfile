FROM maven:3.9.6-amazoncorretto-21-debian

WORKDIR /app

COPY . .

RUN ["mvn", "install", "-Dmaven.test.skip=true"]

ENTRYPOINT ["java", "-jar", "./target/noticeme-0.0.1-SNAPSHOT.jar"]