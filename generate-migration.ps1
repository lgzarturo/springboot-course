param (
    [Parameter(Mandatory=$true)]
    [string]$Description
)

$ScriptDir = $PSScriptRoot
$MigrationDir = "$ScriptDir/src/main/resources/db/migration"
$DockerDir = $ScriptDir

$files = Get-ChildItem -Path $MigrationDir -Filter "V*__*.sql"
$maxVersion = 0
foreach ($file in $files) {
    if ($file.Name -match "^V(\d+)__") {
        $v = [int]$matches[1]
        if ($v -gt $maxVersion) { $maxVersion = $v }
    }
}
$nextVersion = $maxVersion + 1
$cleanDescription = $Description -replace "\s+", "_"
$fileName = "V${nextVersion}__$cleanDescription.sql"
$filePath = "$MigrationDir/$fileName"

Write-Host "
Generando migracion: $fileName" -ForegroundColor Cyan

Write-Host "Levantando base de datos temporal..." -ForegroundColor Green
docker-compose -f "$DockerDir/docker-compose.migration.yml" up -d

Write-Host "Esperando a que la base de datos este lista..." -ForegroundColor Green
$maxWait = 30
$waited = 0
$servicesReady = $false

while ($waited -lt $maxWait) {
    $dbPort = (Test-NetConnection localhost -Port 5433 -WarningAction SilentlyContinue).TcpTestSucceeded

    if ($dbPort) {
        $servicesReady = $true
        break
    }
    Start-Sleep -Seconds 1
    $waited++
}

if (-not $servicesReady) {
    Write-Host "Error: La base de datos no se inicio a tiempo." -ForegroundColor Red
    docker-compose -f "$DockerDir/docker-compose.migration.yml" down
    exit 1
}

Write-Host "Extrayendo esquema desde entidades JPA..." -ForegroundColor Yellow

./gradlew.bat test "--tests=com.lgzarturo.springbootcourse.util.GenerateDdlTest" `
    "-Dspring.profiles.active=migration" "-PincludeMigrationTests=true"

$generatedFile = "$ScriptDir/src/main/resources/db/migration/generated-schema.sql"
if (Test-Path $generatedFile) {
    $content = Get-Content -Path $generatedFile -Raw
    
    $cleanContent = $content -replace ' check \(.*? in \(.*?\)\)', ''
    
    if ($cleanContent.Trim().Length -gt 10) {
        Set-Content -Path $generatedFile -Value $cleanContent
        Move-Item $generatedFile $filePath -Force
        $scriptSuccess = $true
    } else {
        Write-Host "No se detectaron cambios significativos en las entidades. No se creara el archivo de migracion." -ForegroundColor Yellow
        Remove-Item $generatedFile
        $scriptSuccess = $false
    }
} else {
    $scriptSuccess = $false
}

Write-Host "Deteniendo base de datos temporal..." -ForegroundColor Green
docker-compose -f "$DockerDir/docker-compose.migration.yml" down

if ($scriptSuccess) {
    Write-Host "
Exito! Archivo generado en: $filePath" -ForegroundColor Cyan
} else {
    if (-not (Test-Path $generatedFile)) {
         Write-Host "
Informacion: No hubo cambios que requieran migracion." -ForegroundColor Cyan
    } else {
         Write-Host "
Error: No se pudo generar el archivo o esta vacio." -ForegroundColor Red
    }
}
