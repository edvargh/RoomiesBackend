# ---------- build stage ----------
FROM maven:3.9.7-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# ---------- runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/Roomies-*.jar app.jar

# JVM opts: sane defaults for container
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
