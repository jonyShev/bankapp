# ===== Build stage =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
ARG MODULE
RUN mvn -q -DskipTests -pl ${MODULE} -am package

# ===== Run stage =====
FROM eclipse-temurin:21-jre
WORKDIR /app
ARG MODULE
COPY --from=build /app/${MODULE}/target/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]