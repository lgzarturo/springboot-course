#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
    echo "Usage: $0 <description>"
    echo "Example: $0 'add_users_table'"
    exit 1
fi

DESCRIPTION="$1"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MIGRATION_DIR="$SCRIPT_DIR/src/main/resources/db/migration"
GENERATED_FILE="$MIGRATION_DIR/generated-schema.sql"

# Calculate next version
MAX_VERSION=0
for file in "$MIGRATION_DIR"/V*__*.sql; do
    [[ -f "$file" ]] || continue
    basename "$file" | grep -oP '(?<=^V)\d+' | while read -r v; do
        [[ $v -gt $MAX_VERSION ]] && MAX_VERSION=$v
    done
done

# Recompute MAX_VERSION without subshell
MAX_VERSION=0
for file in "$MIGRATION_DIR"/V*__*.sql; do
    [[ -f "$file" ]] || continue
    v=$(basename "$file" | grep -oP '(?<=^V)\d+' || true)
    if [[ -n "$v" && "$v" -gt "$MAX_VERSION" ]]; then
        MAX_VERSION=$v
    fi
done

NEXT_VERSION=$((MAX_VERSION + 1))
CLEAN_DESCRIPTION="${DESCRIPTION// /_}"
FILE_NAME="V${NEXT_VERSION}__${CLEAN_DESCRIPTION}.sql"
FILE_PATH="$MIGRATION_DIR/$FILE_NAME"

echo -e "\nGenerando migracion: $FILE_NAME"

echo "Levantando base de datos temporal..."
docker compose -p migration -f "$SCRIPT_DIR/docker-compose.migration.yml" up -d

cleanup() {
    echo "Deteniendo base de datos temporal..."
    docker compose -p migration -f "$SCRIPT_DIR/docker-compose.migration.yml" down
}
trap cleanup EXIT

echo "Esperando a que la base de datos este lista..."
MAX_WAIT=30
WAITED=0
while [[ $WAITED -lt $MAX_WAIT ]]; do
    if (echo > /dev/tcp/localhost/5433) 2>/dev/null; then
        break
    fi
    sleep 1
    WAITED=$((WAITED + 1))
done

if [[ $WAITED -ge $MAX_WAIT ]]; then
    echo "Error: La base de datos no se inicio a tiempo." >&2
    exit 1
fi

echo "Extrayendo esquema desde entidades JPA..."
GRADLE_CMD="./gradlew"
[[ "$(uname)" == "Darwin" || "$(uname)" == "Linux" ]] && GRADLE_CMD="./gradlew"

"$GRADLE_CMD" test \
    "--tests=com.lgzarturo.springbootcourse.util.GenerateDdlTest" \
    "-Dspring.profiles.active=migration" \
    "-PincludeMigrationTests=true"

SCRIPT_SUCCESS=false

if [[ -f "$GENERATED_FILE" ]]; then
    # Remove check constraints (e.g. "check (status in ('A', 'B'))")
    CLEAN_CONTENT=$(sed -E "s/ check \([^)]*in \([^)]*\)\)//g" "$GENERATED_FILE")

    TRIMMED=$(echo "$CLEAN_CONTENT" | tr -d '[:space:]')
    if [[ ${#TRIMMED} -gt 10 ]]; then
        echo "$CLEAN_CONTENT" > "$GENERATED_FILE"
        mv "$GENERATED_FILE" "$FILE_PATH"
        SCRIPT_SUCCESS=true
    else
        echo "No se detectaron cambios significativos en las entidades. No se creara el archivo de migracion."
        rm -f "$GENERATED_FILE"
    fi
fi

if $SCRIPT_SUCCESS; then
    echo -e "\nExito! Archivo generado en: $FILE_PATH"
else
    if [[ ! -f "$GENERATED_FILE" ]]; then
        echo -e "\nInformacion: No hubo cambios que requieran migracion."
    else
        echo -e "\nError: No se pudo generar el archivo o esta vacio." >&2
        exit 1
    fi
fi
