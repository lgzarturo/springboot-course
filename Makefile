# =============================================================================
# Spring Boot Course — Makefile
# =============================================================================
# Uso: make <target> [ARGS...]
# Ejemplo: make migrate DESC="add_users_table"
# =============================================================================

# Detección de sistema operativo y entorno
ifeq ($(OS),Windows_NT)
    # Windows
    ifneq ($(findstring sh,$(SHELL)),)
        # Shell POSIX en Windows (Git Bash, MSYS2, MinGW, Cygwin)
        GRADLEW := ./gradlew
        MIGRATION_SCRIPT := ./generate-migration.sh
        COPY_ENV := if [ ! -f .env ]; then cp .env.example .env && echo "$(GREEN).env creado desde .env.example$(RESET)"; fi
    else
        # CMD.EXE o PowerShell nativo
        GRADLEW := gradlew.bat
        MIGRATION_SCRIPT := powershell -ExecutionPolicy Bypass -File generate-migration.ps1 -Description
        COPY_ENV := if not exist .env (copy .env.example .env >nul && echo $(GREEN).env creado desde .env.example$(RESET))
    endif
else
    # Linux / macOS / Unix
    GRADLEW := ./gradlew
    MIGRATION_SCRIPT := ./generate-migration.sh
    COPY_ENV := if [ ! -f .env ]; then cp .env.example .env && echo "$(GREEN).env creado desde .env.example$(RESET)"; fi
endif

# Colores (compatibles con Linux, macOS y Windows)
ESC := $(shell printf '\033' 2>/dev/null)
ifneq ($(ESC),)
    CYAN   := $(ESC)[0;36m
    GREEN  := $(ESC)[0;32m
    YELLOW := $(ESC)[0;33m
    RESET  := $(ESC)[0m
else
    CYAN   :=
    GREEN  :=
    YELLOW :=
    RESET  :=
endif

.DEFAULT_GOAL := help

# =============================================================================
# AYUDA
# =============================================================================
.PHONY: help
help: ## Muestra esta ayuda
	@echo ""
	@echo "$(CYAN)Spring Boot Course — Comandos disponibles$(RESET)"
	@echo "$(CYAN)==========================================$(RESET)"
	@awk 'BEGIN {FS = ":.*##"} /^[a-zA-Z_-]+:.*##/ { printf "  $(GREEN)%-22s$(RESET) %s\n", $$1, $$2 }' $(MAKEFILE_LIST) | sort
	@echo ""
	@echo "$(YELLOW)Variables:$(RESET)"
	@echo "  DESC   Descripción de la migración  (make migrate DESC='add_users')"
	@echo "  VER    Versión de la migración       (make create-migration VER=2 DESC='add_rooms')"
	@echo ""

# =============================================================================
# BUILD
# =============================================================================
.PHONY: build
build: ## Compila el proyecto
	$(GRADLEW) clean build

.PHONY: compile
compile: ## Compila sin limpiar ni ejecutar tests
	$(GRADLEW) classes testClasses

.PHONY: version
version: ## Muestra la versión actual del proyecto
	$(GRADLEW) printVersion

# =============================================================================
# EJECUCIÓN
# =============================================================================
.PHONY: run
run: ## Ejecuta la aplicación (perfil dev)
	$(GRADLEW) bootRun

.PHONY: run-dev
run-dev: ## Ejecuta con perfil dev explícito
	$(GRADLEW) bootRun --args='--spring.profiles.active=dev'

.PHONY: run-prod
run-prod: ## Ejecuta con perfil prod
	$(GRADLEW) bootRun --args='--spring.profiles.active=prod'

# =============================================================================
# TESTS
# =============================================================================
.PHONY: test
test: ## Ejecuta todos los tests
	$(GRADLEW) test

.PHONY: test-class
test-class: ## Ejecuta una clase de test (make test-class CLASS=HotelServiceTest)
	$(GRADLEW) test --tests "com.lgzarturo.springbootcourse.$(CLASS)"

.PHONY: coverage
coverage: ## Ejecuta tests y genera reporte JaCoCo (build/reports/jacoco/)
	$(GRADLEW) jacocoTestReport

.PHONY: coverage-check
coverage-check: ## Verifica que la cobertura supere el 85%
	$(GRADLEW) jacocoTestCoverageVerification

# =============================================================================
# CALIDAD DE CÓDIGO
# =============================================================================
.PHONY: lint
lint: ## Verifica estilo con ktlint y detekt
	$(GRADLEW) checkCodeStyle

.PHONY: format
format: ## Aplica correcciones automáticas (ktlint + detekt)
	$(GRADLEW) formatCode

.PHONY: fix
fix: ## Aplica todas las correcciones automáticas disponibles
	$(GRADLEW) fixAll

.PHONY: quality
quality: ## Ejecuta lint + tests + cobertura (gate completo)
	$(GRADLEW) codeQuality

.PHONY: lint-report
lint-report: ## Genera reportes de análisis en build/reports/
	$(GRADLEW) lintReport

# =============================================================================
# BASE DE DATOS
# =============================================================================
.PHONY: ddl
ddl: ## Genera DDL desde entidades JPA (build/schema-create.sql)
	$(GRADLEW) generateDDL

.PHONY: diff-migration
diff-migration: ## Muestra instrucciones para generar migración incremental
	$(GRADLEW) diffMigration

.PHONY: create-migration
create-migration: ## Crea migración Flyway desde DDL (make create-migration VER=2 DESC="add_rooms")
ifndef VER
	$(error Variable VER no definida. Uso: make create-migration VER=2 DESC="add_rooms")
endif
ifndef DESC
	$(error Variable DESC no definida. Uso: make create-migration VER=2 DESC="add_rooms")
endif
	$(GRADLEW) createMigration -Pmigration_version=$(VER) -Pdescription="$(DESC)"

.PHONY: migrate
migrate: ## Genera migración automática desde entidades JPA (make migrate DESC="add_users")
ifndef DESC
	$(error Variable DESC no definida. Uso: make migrate DESC="add_users_table")
endif
	$(MIGRATION_SCRIPT) "$(DESC)"

# =============================================================================
# DOCKER
# =============================================================================
.PHONY: docker-up
docker-up: ## Levanta los servicios de Docker (base de datos)
	docker compose up -d

.PHONY: docker-down
docker-down: ## Detiene los servicios de Docker
	docker compose down

.PHONY: docker-logs
docker-logs: ## Muestra logs de los contenedores
	docker compose logs -f

.PHONY: docker-ps
docker-ps: ## Lista contenedores activos
	docker compose ps

# =============================================================================
# ATAJOS COMBINADOS
# =============================================================================
.PHONY: ci
ci: ## Simula el pipeline de CI completo (build + quality)
	$(GRADLEW) clean build codeQuality

.PHONY: setup
setup: ## Configura el entorno local (copia .env y levanta Docker)
	@$(COPY_ENV)
	$(MAKE) docker-up

.PHONY: reset
reset: ## Limpia build artifacts y detiene Docker
	$(GRADLEW) clean
	$(MAKE) docker-down
