FROM eclipse-temurin:17-jre-noble
ARG JAR_FILE=directory-server/target/*.jar
COPY ${JAR_FILE} app.jar
# Directory Server
#EXPOSE 5432
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]