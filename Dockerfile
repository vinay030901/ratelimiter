FROM eclipse-temurin:17-jdk as build
WORKDIR /app

# copy and build using maven wrapper
COPY . .
RUN ./mvnw clean package -DskipTests

# create runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app

# copy the jar from build stage
COPY --from=build /app/target/*.jar /app/app.jar

# run the app
CMD ["java", "-jar", "app.jar"]
