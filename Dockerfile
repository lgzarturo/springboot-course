# Gran Hotel Pokemon — imagen multi-stage de la aplicación (BC-014).
# Construir: docker build -t hotel-pokemon .
# Ejecutar:  docker compose up (ver docker-compose.yml)

# ---------------------------------------------------------------------------
# Stage 1: builder — compila con el Gradle wrapper del repo y deriva un
# runtime JDK 25 mínimo con jlink (compilado musl, corre en Alpine).
# ---------------------------------------------------------------------------
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /workspace

# Wrapper primero para aprovechar la caché de capas.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew

# Fuentes de la aplicación y configuración de calidad (solo evaluación).
COPY config ./config
COPY src ./src

# JAR layered booteable sin tests (CI / `make quality` ya los ejecuta;
# Testcontainers no debe correr dentro del build de la imagen).
RUN ./gradlew bootJar -x test --no-daemon --console=plain

# Runtime Java mínimo: módulos derivados de `jdeps` sobre el JAR más los que
# Spring, Postgres y Sentry necesitan por reflexión/TLS/SASL en runtime.
# Validado arrancando la app y consultando /actuator/health (review BC-014).
RUN jlink \
    --strip-debug \
    --no-header-files \
    --no-man-pages \
    --compress=zip-9 \
    --add-modules java.base,java.compiler,java.desktop,java.instrument,java.management,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql,java.sql.rowset,java.xml,jdk.charsets,jdk.crypto.ec,jdk.jfr,jdk.management,jdk.net,jdk.unsupported \
    --output /opt/jre-slim

# Extrae las capas Spring Boot según BOOT-INF/layers.idx (Spring Boot 4 eliminó
# el jarmode `layertools`; `unzip` replica sus 4 directorios). Excluye el JAR
# `-plain`, que no tiene layout layered.
RUN BOOT_JAR=$(ls build/libs/*.jar | grep -v -- '-plain.jar') \
    && mkdir -p /opt/app-layers/dependencies /opt/app-layers/spring-boot-loader \
        /opt/app-layers/snapshot-dependencies /opt/app-layers/application \
    && unzip -q "$BOOT_JAR" 'BOOT-INF/lib/*' -d /tmp/jar-libs \
    && mkdir -p /opt/app-layers/snapshot-dependencies/BOOT-INF/lib /opt/app-layers/dependencies/BOOT-INF \
    && mv /tmp/jar-libs/BOOT-INF/lib/*-SNAPSHOT.jar /opt/app-layers/snapshot-dependencies/BOOT-INF/lib/ 2>/dev/null || true \
    && mv /tmp/jar-libs/BOOT-INF/lib /opt/app-layers/dependencies/BOOT-INF/ \
    && unzip -q "$BOOT_JAR" 'org/*' -d /opt/app-layers/spring-boot-loader \
    && unzip -q "$BOOT_JAR" 'BOOT-INF/classes/*' 'BOOT-INF/classpath.idx' 'BOOT-INF/layers.idx' 'META-INF/*' -d /opt/app-layers/application

# ---------------------------------------------------------------------------
# Stage 2: runtime — Alpine + JRE slim + capas de la app, usuario no-root.
# Sin secretos: DB/JWT solo llegan por entorno en docker-compose.yml.
# ---------------------------------------------------------------------------
FROM alpine:3.22 AS runtime

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /opt/app

COPY --from=builder /opt/jre-slim /opt/jre-slim
COPY --from=builder --chown=appuser:appgroup /opt/app-layers/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /opt/app-layers/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /opt/app-layers/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /opt/app-layers/application/ ./

# Hibernate (schema-export) y Logback (file appender) del perfil dev escriben
# rutas relativas; el directorio de trabajo debe pertenecer a appuser.
RUN mkdir -p /opt/app/build /opt/app/logs && chown appuser:appgroup /opt/app /opt/app/build /opt/app/logs

ENV SPRING_PROFILES_ACTIVE="dev"

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

USER appuser

ENTRYPOINT ["/opt/jre-slim/bin/java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "org.springframework.boot.loader.launch.JarLauncher"]
