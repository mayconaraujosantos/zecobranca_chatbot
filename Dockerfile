# Multi-stage build para otimizar tamanho da imagem
FROM gradle:8.12.0-jdk21-alpine AS builder

# Definir diretório de trabalho
WORKDIR /app

# Copiar arquivos de configuração do Gradle primeiro (para cache)
COPY build.gradle.kts settings.gradle.kts gradle.properties* ./
COPY libs.versions.toml ./
COPY gradle/ gradle/

# Baixar dependências (será cacheado se os arquivos acima não mudarem)
RUN gradle dependencies --no-daemon

# Copiar código fonte
COPY src/ src/

# Compilar aplicação
RUN gradle build -x test --no-daemon

# Estágio final - Runtime
FROM amazoncorretto:17-alpine

# Instalar dumb-init para melhor handling de sinais
RUN apk add --no-cache dumb-init

# Criar usuário não-root para segurança
RUN addgroup -g 1001 -S appgroup && \
    adduser -S appuser -u 1001 -G appgroup

# Definir diretório de trabalho
WORKDIR /app

# Copiar JAR da fase de build
COPY --from=builder /app/build/libs/*.jar app.jar

# Alterar ownership para usuário não-root
RUN chown -R appuser:appgroup /app

# Mudar para usuário não-root
USER appuser

# Definir variáveis de ambiente
ENV JAVA_OPTS="-Xmx512m -Xms256m" \
    SERVER_PORT=8080

# Expor porta (Railway usa a variável PORT)
EXPOSE 8080

# Comando de entrada com dumb-init
ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]