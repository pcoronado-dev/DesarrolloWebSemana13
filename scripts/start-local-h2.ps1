# Levanta Eureka, los cuatro microservicios y el Gateway usando H2 en memoria.
# Util para demostrar el laboratorio cuando no se dispone de MySQL.
#
# Uso:
#   powershell -ExecutionPolicy Bypass -File .\scripts\start-local-h2.ps1
#
# Los procesos quedan en segundo plano. Para detenerlos:
#   powershell -ExecutionPolicy Bypass -File .\scripts\stop-local.ps1

$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$micro = Join-Path $root 'microservicios'
$logs = Join-Path $env:TEMP 'opencode\s13'
New-Item -ItemType Directory -Force -Path $logs | Out-Null

function Start-Servicio {
    param(
        [string]$Nombre,
        [int]$Puerto,
        [string]$BaseDatos,
        [string]$Modulo
    )

    $carpeta = Join-Path $micro $Modulo
    $log = Join-Path $logs "$Nombre.log"
    $err = Join-Path $logs "$Nombre.err.log"

    $env:PROFILE_ACTIVE = 'dev'
    $env:SERVER_PORT = "$Puerto"
    $env:EUREKA_URL = 'http://localhost:8761/eureka'

    if ($BaseDatos) {
        $env:SPRING_DATASOURCE_URL = "jdbc:h2:mem:$BaseDatos;MODE=MySQL;DB_CLOSE_DELAY=-1"
        $env:SPRING_DATASOURCE_DRIVER_CLASS_NAME = 'org.h2.Driver'
        $env:SPRING_DATASOURCE_USERNAME = 'sa'
        $env:SPRING_DATASOURCE_PASSWORD = ''
        $env:SPRING_JPA_DATABASE_PLATFORM = 'org.hibernate.dialect.H2Dialect'
        $env:SPRING_JPA_HIBERNATE_DDL_AUTO = 'update'
    }

    $argumentos = "/c `"`"$carpeta\mvnw.cmd`" spring-boot:run -Dspring-boot.run.useTestClasspath=true > `"$log`" 2> `"$err`"`""
    Start-Process -FilePath 'cmd.exe' -ArgumentList $argumentos -WorkingDirectory $carpeta -WindowStyle Hidden | Out-Null
    Write-Host ("Iniciando {0,-18} puerto {1}" -f $Nombre, $Puerto)
}

Write-Host '==> Iniciando eureka-server'
Start-Servicio -Nombre 'eureka-server' -Puerto 8761 -Modulo 'eureka-server'

$limite = (Get-Date).AddMinutes(3)
while ((Get-Date) -lt $limite) {
    try {
        $r = Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:8761/eureka/apps' -TimeoutSec 3
        if ($r.StatusCode -eq 200) { break }
    } catch { Start-Sleep -Seconds 2 }
}
Write-Host 'Eureka disponible en http://localhost:8761'

Write-Host '==> Iniciando microservicios'
Start-Servicio -Nombre 'categoria-service' -Puerto 8082 -BaseDatos 'bd_categoria_dev' -Modulo 'categoria-service'
Start-Servicio -Nombre 'cliente-service'   -Puerto 8081 -BaseDatos 'bd_cliente_dev'   -Modulo 'cliente-service'
Start-Servicio -Nombre 'producto-service'  -Puerto 8083 -BaseDatos 'bd_producto_dev'  -Modulo 'producto-service'
Start-Servicio -Nombre 'pedido-service'    -Puerto 8084 -BaseDatos 'bd_pedido_dev'    -Modulo 'pedido-service'

Write-Host '==> Iniciando gateway-service'
Start-Servicio -Nombre 'gateway-service' -Puerto 8080 -Modulo 'gateway-service'

Write-Host ''
Write-Host "Logs en: $logs"
Write-Host 'Espera unos segundos y verifica http://localhost:8761 y http://localhost:8080/actuator/health'
