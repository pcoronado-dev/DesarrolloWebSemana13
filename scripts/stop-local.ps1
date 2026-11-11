# Detiene los procesos Java de los seis servicios del laboratorio.
# Solo mata procesos que esten escuchando en los puertos del proyecto.

$puertos = 8080, 8081, 8082, 8083, 8084, 8761

foreach ($puerto in $puertos) {
    $conexiones = Get-NetTCPConnection -State Listen -LocalPort $puerto -ErrorAction SilentlyContinue
    foreach ($conexion in $conexiones) {
        try {
            $proceso = Get-Process -Id $conexion.OwningProcess -ErrorAction Stop
            Write-Host ("Deteniendo {0} (PID {1}) en puerto {2}" -f $proceso.ProcessName, $proceso.Id, $puerto)
            Stop-Process -Id $proceso.Id -Force
        } catch {
            Write-Warning "No se pudo detener el proceso del puerto $puerto"
        }
    }
}
