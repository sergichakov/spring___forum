FROM eclipse-temurin:17-jre-noble
ARG JAR_FILE=authorization-server/target/*.jar
COPY ${JAR_FILE} app.jar
# Authorization server
EXPOSE 9000
#EXPOSE 5432
ENTRYPOINT ["java", "-jar", "/app.jar", "-Dspring.profiles.active=prod"]