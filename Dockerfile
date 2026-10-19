FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /tasks
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /tasks
COPY --from=build /tasks/target/tasks.jar tasks.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "tasks.jar"]