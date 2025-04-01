FROM eclipse-temurin:17-jre-noble
ARG JAR_FILE=resource-service/target/*.jar
COPY ${JAR_FILE} app.jar
# resource service
EXPOSE 8090
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]