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
	@echo "  DESC        Descripción de la migración  (make migrate DESC='add_users')"
	@echo "  VER         Versión de la migración       (make create-migration VER=2 DESC='add_rooms')"
	@echo "  TYPE        Tipo de incremento SemVer     (make release TYPE=minor)"
	@echo "  VERSION     Versión explícita             (make release VERSION=0.1.0)"
	@echo "  NOTES       Notas del release             (make release NOTES='Notas...')"
	@echo "  NOTES_FILE  Archivo de notas del release  (make release NOTES_FILE='release.md')"
	@echo "  ALLOW_DIRTY Permitir release con git sucio (make release ALLOW_DIRTY=true)"
	@echo "  SKIP_TESTS  Omitir suite de tests         (make release SKIP_TESTS=true)"
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

# =============================================================================
# VERSIONADO Y RELEASES
# =============================================================================
.PHONY: version
version: ## Muestra la versión actual del proyecto
	$(GRADLEW) printVersion

.PHONY: whatsnext
whatsnext: ## Muestra sugerencias de próximas versiones SemVer (patch, minor, major)
	$(GRADLEW) whatsnext

.PHONY: release
release: ## Crea release completo (tests, versión, changelog, commit y git tag)
	$(GRADLEW) createRelease $(if $(TYPE),-Ptype=$(TYPE),) $(if $(VERSION),-PnewVersion=$(VERSION),) $(if $(NOTES),-Pnotes="$(NOTES)",) $(if $(NOTES_FILE),-PnotesFile="$(NOTES_FILE)",) $(if $(ALLOW_DIRTY),-PallowDirty=$(ALLOW_DIRTY),) $(if $(SKIP_TESTS),-PskipTests=$(SKIP_TESTS),)

.PHONY: release-patch
release-patch: ## Crea release PATCH (0.0.x) con tests, tag y changelog
	$(GRADLEW) createRelease -Ptype=patch $(if $(NOTES),-Pnotes="$(NOTES)",) $(if $(NOTES_FILE),-PnotesFile="$(NOTES_FILE)",) $(if $(ALLOW_DIRTY),-PallowDirty=$(ALLOW_DIRTY),) $(if $(SKIP_TESTS),-PskipTests=$(SKIP_TESTS),)

.PHONY: release-minor
release-minor: ## Crea release MINOR (0.x.0) con tests, tag y changelog
	$(GRADLEW) createRelease -Ptype=minor $(if $(NOTES),-Pnotes="$(NOTES)",) $(if $(NOTES_FILE),-PnotesFile="$(NOTES_FILE)",) $(if $(ALLOW_DIRTY),-PallowDirty=$(ALLOW_DIRTY),) $(if $(SKIP_TESTS),-PskipTests=$(SKIP_TESTS),)

.PHONY: release-major
release-major: ## Crea release MAJOR (x.0.0) con tests, tag y changelog
	$(GRADLEW) createRelease -Ptype=major $(if $(NOTES),-Pnotes="$(NOTES)",) $(if $(NOTES_FILE),-PnotesFile="$(NOTES_FILE)",) $(if $(ALLOW_DIRTY),-PallowDirty=$(ALLOW_DIRTY),) $(if $(SKIP_TESTS),-PskipTests=$(SKIP_TESTS),)

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
quality: ## Gate completo fail-fast: KtLint → Detekt → tests → JaCoCo verificación (LINE 85%) → build
	$(GRADLEW) codeQuality

.PHONY: verify-quality
verify-quality: ## Verifica el cableado del gate (orden, umbral LINE 85%, docs)
	@grep -A 3 'tasks.register("codeQuality")' build.gradle.kts | grep 'dependsOn' | grep -q 'ktlintCheck.*detekt.*test.*jacocoTestReport.*jacocoTestCoverageVerification.*build'
	@test $$(grep -c 'mustRunAfter' build.gradle.kts) -ge 5
	@grep -A 8 'tasks.jacocoTestCoverageVerification' build.gradle.kts | grep -qi 'counter.*LINE'
	@grep -A 8 'tasks.jacocoTestCoverageVerification' build.gradle.kts | grep -q '0\.85'
	@grep -q 'ignoreFailures.set(false)' build.gradle.kts
	@grep -A 4 'tasks.named<Detekt>("detekt")' build.gradle.kts | grep -q 'autoCorrect = false'
	@grep -A 5 'tasks.jacocoTestReport' build.gradle.kts | grep -q 'html.required.set(true)'
	@grep -q 'make quality' README.md
	@echo "✓ Gate wiring OK: orden declarado, LINE 85%, sin auto-corrección, docs presentes"

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
