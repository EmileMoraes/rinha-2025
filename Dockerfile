# --- Stage 1: Build the application ---
FROM maven:3.9.6-eclipse-temurin-17 AS build-app

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# --- Stage 2: Create the final runtime image ---
FROM eclipse-temurin:17-jre

WORKDIR /app
# IMPORTANT: Change 'builder' to 'build-app' in all these lines:
COPY --from=build-app /app/target/dependency/BOOT-INF/lib /app/BOOT-INF/lib
COPY --from=build-app /app/target/dependency/META-INF /app/META-INF
COPY --from=build-app /app/target/dependency/org /app/org
COPY --from=build-app /app/target/*.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
EXPOSE 8686