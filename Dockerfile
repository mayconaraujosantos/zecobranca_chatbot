# Etapa de build
FROM gradle:8.12.0-jdk17-alpine AS builder

WORKDIR /app

# Copiar arquivos do Gradle na ordem correta
COPY gradle/ gradle/
COPY gradlew gradlew.bat ./
COPY settings.gradle.kts .
COPY gradle.properties .
COPY app/build.gradle.kts app/

# Dar permissão de execução ao gradlew e baixar dependências
RUN chmod +x gradlew && \
    ./gradlew dependencies --no-daemon || return 0

# Copiar código fonte
COPY app/src/ app/src/

# Compilar aplicação
RUN ./gradlew clean build --no-daemon

# Etapa final - Runtime
FROM eclipse-temurin:17-jre-alpine

# Instalar curl para healthcheck mais confiável
RUN apk add --no-cache curl

# Criar usuário não-root com Alpine tools
RUN addgroup -g 1001 -S appgroup && \
    adduser -S appuser -u 1001 -G appgroup

WORKDIR /app

# Copiar jar do builder e ajustar permissões
COPY --from=builder /app/app/build/libs/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

# Configurar variáveis de ambiente padrão (serão sobrescritas pelo Railway)
ENV PORT=8080
ENV JAVA_OPTS="-Xmx512m -Xms256m"

EXPOSE 8080

# Healthcheck usando curl com 0.0.0.0 (mais confiável para Railway)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://0.0.0.0:${PORT}/health || exit 1

# Comando de inicialização que usa a variável PORT do Railway
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
