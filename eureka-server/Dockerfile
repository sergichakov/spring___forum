FROM eclipse-temurin:17-jre-noble
ARG JAR_FILE=eureka-server/target/*.jar
COPY ${JAR_FILE} app.jar
# eureka-server
EXPOSE 8761

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]
#ENTRYPOINT ["java", "-jar", "/app.jar"]





#FROM eclipse-temurin:17.0.5_8-jre-focal as builder
#WORKDIR extracted
#ADD ./build/libs/*.jar app.jar
#RUN java -Djarmode=layertools -jar app.jar extract
#
#FROM eclipse-temurin:17.0.5_8-jre-focal
#WORKDIR application
#COPY --from=builder extracted/dependencies/ ./
#COPY --from=builder extracted/spring-boot-loader/ ./
#COPY --from=builder extracted/snapshot-dependencies/ ./
#COPY --from=builder extracted/application/ ./
#
#EXPOSE 8761
#
#ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
