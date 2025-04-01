FROM eclipse-temurin:17-jre-noble
ARG JAR_FILE=authorization-server/target/*.jar
COPY ${JAR_FILE} app.jar
# Authorization server
EXPOSE 9000
#EXPOSE 5432
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]
#HEALTHCHECK --interval=30s --timeout=5s CMD ["/usr/bin/curl", "-f", "http://localhost:9000/oauth2/ready"]