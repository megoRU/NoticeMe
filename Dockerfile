# Stage 1: build
FROM maven:3.9.11-amazoncorretto-25-al2023 AS builder
WORKDIR /app

# Сначала только pom.xml — чтобы кэшировались зависимости
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Потом исходники
COPY src ./src
RUN mvn -B package -DskipTests

# Stage 2: runtime
FROM amazoncorretto:25-jdk
ENV LANG=C.UTF-8
WORKDIR /app

COPY --from=builder /app/target/noticeme-0.0.1-SNAPSHOT.jar ./noticeme.jar

ENTRYPOINT ["java", "-jar", "noticeme.jar"]