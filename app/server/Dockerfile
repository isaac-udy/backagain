# The image only packages what Gradle already built: `./gradlew :app:server:dockerContext` stages
# the fat jar, the web bundle and this file into app/server/build/docker, which is the build context.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
RUN addgroup -S backagain && adduser -S backagain -G backagain

COPY server-all.jar app.jar
COPY web/ web/

USER backagain
ENV PORT=8080 \
    BACKAGAIN_WEB_DIR=/app/web
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
