FROM amazoncorretto:17
WORKDIR /app

# Copy the pre-built JAR
COPY target/gym-crm-system-1.0.0.jar app.jar

# Expose port
EXPOSE 8080

# JWT secret must be passed as environment variable when running
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]

# Optional: default profile
ENV SPRING_PROFILES_ACTIVE=docker

