FROM smetecnologia/maven:3.9.6-openjdk-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# ЭТАП 2 : Запуск приложения
FROM smetecnologia/maven:3.9.6-openjdk-21 AS prom

WORKDIR /app

COPY --from=build /app/target/idm-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
