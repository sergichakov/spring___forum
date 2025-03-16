FROM eclipse-temurin:17-jre-noble
ARG JAR_FILE=topic-web/target/*.jar
COPY ${JAR_FILE} app.jar
# Topic Web
EXPOSE 18071
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]