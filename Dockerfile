FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/bank-rest-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]