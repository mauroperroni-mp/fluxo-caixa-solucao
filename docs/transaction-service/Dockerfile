# Etapa 1: Build da aplicação utilizando Maven e Java 17 (Multi-stage build)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia os arquivos do projeto para o container e executa o build sem rodar os testes
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Execução com JRE leve e otimizada
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copia apenas o JAR gerado na etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta do transaction-service
EXPOSE 8080

# Executa a aplicação Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
