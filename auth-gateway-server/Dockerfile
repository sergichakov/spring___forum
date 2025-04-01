FROM eclipse-temurin:17-jre-noble
ARG JAR_FILE=auth-gateway-server/target/*.jar
COPY ${JAR_FILE} app.jar
# Gateway
EXPOSE 8080
#EXPOSE 9000
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]