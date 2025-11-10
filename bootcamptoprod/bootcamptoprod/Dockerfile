FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn ./.mvn
COPY src ./src

RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /app/uploads
VOLUME ["/app/uploads"]

EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]
