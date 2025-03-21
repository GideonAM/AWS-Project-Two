FROM openjdk:17

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} backend.jar

ENTRYPOINT ["java", "-jar", "/backend.jar"]