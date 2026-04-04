# --- Настройки путей ---
$CertsDir = Join-Path $PSScriptRoot "..\..\nginx\certs"
$ProjectName = "PulsE"

Write-Host "[Initializing $ProjectName Development Environment]" -ForegroundColor Cyan

# --- Функция для генерации SSL сертификатов ---
function Setup-Certs {
    Write-Host "`n[1/1] Setting up SSL Certificates..." -ForegroundColor Yellow
    
    # Проверка наличия mkcert
    if (-not (Get-Command mkcert -ErrorAction SilentlyContinue)) {
        Write-Host "[ERROR] mkcert not found! Please install it (winget install mkcert)." -ForegroundColor Red
        return
    }

    # Создание папки, если её нет
    if (-not (Test-Path $CertsDir)) {
        New-Item -Path $CertsDir -ItemType Directory | Out-Null
        Write-Host "Created directory: $CertsDir" -ForegroundColor Gray
    }

    # Установка локального CA (Root)
    mkcert -install

    # Генерация сертификатов с фиксированными именами для Nginx
    $CertFile = Join-Path $CertsDir "localhost.pem"
    $KeyFile = Join-Path $CertsDir "localhost-key.pem"
    
    mkcert -cert-file $CertFile -key-file $KeyFile localhost 127.0.0.1 ::1
    
    Write-Host "Certificates generated successfully" -ForegroundColor Green
}

# --- Запуск этапов инициализации ---
try {
    Setup-Certs
    
    # Сюда в будущем добавишь:
    # Setup-Database
    # Setup-EnvironmentFiles
    
    Write-Host "`n=== Initialization Completed Successfully! ===" -ForegroundColor Cyan
}
catch {
    Write-Host "`n[FATAL ERROR] Something went wrong: $_" -ForegroundColor Red
}