FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /src
COPY . .
RUN mvn -B -pl services/trade-service -am package -DskipTests
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 10001 app && mkdir -p /app/uploads && chown -R app /app
COPY --from=build /src/services/trade-service/target/*-exec.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
