FROM openjdk:22-jdk-slim
LABEL authors="udayhegde"
WORKDIR /app
COPY build/libs/Quicksilver-1.0-SNAPSHOT.jar Quicksilver.jar
CMD ["java", "-jar", "Quicksilver.jar"]