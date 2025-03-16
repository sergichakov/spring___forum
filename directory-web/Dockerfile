FROM eclipse-temurin:17-jre-noble
ARG JAR_FILE=directory-web/target/*.jar
COPY ${JAR_FILE} app.jar
# Directory Web
EXPOSE 18081
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]