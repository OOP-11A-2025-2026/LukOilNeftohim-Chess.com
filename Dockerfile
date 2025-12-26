
  FROM maven:3.9.1-eclipse-temurin-17 AS build

  WORKDIR /app
  COPY pom.xml .
  COPY src ./src
  
  RUN mvn clean package shade:shade
  
  FROM eclipse-temurin:17-jre
  
  WORKDIR /app
  
  COPY --from=build /app/target/chess.jar chess.jar
  
  ENTRYPOINT ["java", "-jar", "chess.jar"]
