# Build multi-stage: o JDK e o Maven ficam só no estágio de build; a imagem final leva apenas o JRE e o jar.
#
# Uso:
#   docker build -t desafio .
#   docker run --rm -p 8080:8080 desafio
#   → http://localhost:8080/swagger-ui.html

# ---------- estágio 1: build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Dependências numa camada separada para aproveitar o cache enquanto o pom não muda.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Testes rodam fora do build da imagem (mvnw test).
RUN mvn -B -q package -DskipTests

# ---------- estágio 2: runtime ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Executa sem privilégios de root.
RUN useradd --system --no-create-home appuser
USER appuser

COPY --from=build /app/target/desafio-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
