FROM amazoncorretto:17
RUN mkdir -p /opt/app
WORKDIR /opt/app

# Copy the pre-built JAR
COPY target/gym-crm-system-1.0.0.jar /opt/app

# Expose port
EXPOSE 8080

# JWT secret must be passed as environment variable when running
ENTRYPOINT ["java", "-jar", "gym-crm-system-1.0.0.jar"]
