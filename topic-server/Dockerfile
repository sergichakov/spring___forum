FROM eclipse-temurin:17-jre-noble
ARG JAR_FILE=topic-server/target/*.jar
COPY ${JAR_FILE} app.jar
# Topic Server

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]