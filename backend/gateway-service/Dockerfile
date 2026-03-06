FROM eclipse-temurin:25-jdk-noble

WORKDIR /app

RUN apt-get update && apt-get install -y findutils

COPY gradle/ gradle/
COPY gradlew settings.gradle.kts ./

COPY gateway-service/build.gradle.kts gateway-service/

RUN chmod +x ./gradlew

RUN ./gradlew :gateway:dependencies --no-daemon || true

CMD ["./gradlew", ":gateway-service:bootRun", "--args='--spring.profiles.active=dev'", "-t"]