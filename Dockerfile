# Etapa de build
FROM gradle:8.12.0-jdk21-alpine AS builder

WORKDIR /app

# Copiar arquivos do Gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties* ./
COPY gradle/libs.versions.toml gradle/
COPY gradle/wrapper/ gradle/wrapper/

# Baixar dependências
RUN gradle build -x test --no-daemon || return 0

# Copiar código fonte
COPY src/ src/

# Compilar aplicação
RUN gradle clean build -x test --no-daemon

# Etapa final - Runtime
FROM eclipse-temurin:17-jre-alpine

# Criar usuário não-root
RUN addgroup -g 1001 -S appgroup && \
    adduser -S appuser -u 1001 -G appgroup

WORKDIR /app

# Copiar jar do builder
COPY --from=builder /app/build/libs/*.jar app.jar

USER appuser

# Railway define $PORT automaticamente
ENV JAVA_OPTS="-Xmx512m -Xms256m"

EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
