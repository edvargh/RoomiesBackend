# ---------- build stage ----------
FROM maven:3.9.7-eclipse-temurin-17 AS build
WORKDIR /app

# cache deps first
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# then build sources
COPY src ./src
RUN mvn -q -DskipTests package

# ---------- runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/Roomies-0.0.1-SNAPSHOT.jar /app/app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
EXPOSE 8080

# Railway sets $PORT at runtime; pass it to Spring
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar --server.port=${PORT:-8080}"]
