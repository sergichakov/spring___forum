FROM eclipse-temurin:17-jre-noble
ARG JAR_FILE=post-server/target/*.jar
COPY ${JAR_FILE} app.jar
# Post Server

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]