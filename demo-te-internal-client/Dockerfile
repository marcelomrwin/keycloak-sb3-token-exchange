FROM eclipse-temurin:17-jdk-alpine
COPY maven/src/main/resources/keycloak.cer /certificates/keycloak.cer
COPY maven/target/*.jar /deployments/app.jar
ENV USE_SYSTEM_CA_CERTS=true
RUN keytool -storepass "changeit" -keypass "changeit" -importcert -noprompt -alias keycloak -cacerts -file /certificates/keycloak.cer
EXPOSE 8080
ENTRYPOINT ["java","-Dspring.config.location=/app/config/application.properties","-Dspring.profiles.active=production","-jar","/deployments/app.jar"]