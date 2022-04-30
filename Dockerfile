FROM openjdk:17-jdk-slim

WORKDIR /app
ADD build/libs/PixelJet-*.jar /app/pixeljet.jar

ENTRYPOINT ["java", "-jar", "/app/pixeljet.jar"]
CMD ["--help"]


