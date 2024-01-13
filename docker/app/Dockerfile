FROM khipu/openjdk17-alpine
RUN mkdir -p deploy
WORKDIR /deploy
COPY ./build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]