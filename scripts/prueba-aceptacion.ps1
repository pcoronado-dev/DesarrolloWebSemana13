# Prueba de aceptacion del laboratorio GLAB-S13.
# Verifica que el API Gateway publique los cuatro microservicios a traves de Eureka.
#
# Requisitos: eureka-server, los cuatro microservicios y gateway-service levantados.
# Uso:
#   powershell -ExecutionPolicy Bypass -File .\scripts\prueba-aceptacion.ps1

$ErrorActionPreference = 'Stop'

$gateway = 'http://localhost:8080'
$eureka = 'http://localhost:8761'

$script:ok = 0
$script:fail = 0

function Check {
    param([string]$Nombre, [bool]$Condicion, [string]$Detalle = '')
    if ($Condicion) {
        $script:ok++
        Write-Host ("OK   {0} {1}" -f $Nombre, $Detalle)
    } else {
        $script:fail++
        Write-Host ("FALLO {0} {1}" -f $Nombre, $Detalle)
    }
}

function Invoke-Api {
    param(
        [string]$Metodo,
        [string]$Url,
        $Cuerpo = $null,
        [hashtable]$Cabeceras = @{}
    )

    $parametros = @{
        UseBasicParsing = $true
        Method          = $Metodo
        Uri             = $Url
        TimeoutSec      = 30
        Headers         = $Cabeceras
    }

    if ($null -ne $Cuerpo) {
        $parametros.ContentType = 'application/json'
        $parametros.Body = ($Cuerpo | ConvertTo-Json -Depth 10)
    }

    try {
        $respuesta = Invoke-WebRequest @parametros
        $contenido = $respuesta.Content
        if ($contenido -is [byte[]]) {
            $contenido = [System.Text.Encoding]::UTF8.GetString($contenido)
        }
        return [pscustomobject]@{
            Status = [int]$respuesta.StatusCode
            Body   = $contenido
            Json   = if ($contenido) { $contenido | ConvertFrom-Json } else { $null }
        }
    } catch {
        $respuesta = $_.Exception.Response
        if ($null -eq $respuesta) {
            return [pscustomobject]@{ Status = 0; Body = $_.Exception.Message; Json = $null }
        }
        $lector = New-Object System.IO.StreamReader($respuesta.GetResponseStream())
        $texto = $lector.ReadToEnd()
        return [pscustomobject]@{
            Status = [int]$respuesta.StatusCode
            Body   = $texto
            Json   = if ($texto) { $texto | ConvertFrom-Json } else { $null }
        }
    }
}

Write-Host '============================================================'
Write-Host ' 1. Servicios registrados en Eureka'
Write-Host '============================================================'

$registro = Invoke-Api -Metodo GET -Url "$eureka/eureka/apps" -Cabeceras @{ Accept = 'application/json' }
Check 'Eureka responde 200' ($registro.Status -eq 200)

$aplicaciones = @($registro.Json.applications.application)
$nombres = @($aplicaciones | ForEach-Object { $_.name })

foreach ($servicio in 'CATEGORIA-SERVICE', 'CLIENTE-SERVICE', 'PRODUCTO-SERVICE', 'PEDIDO-SERVICE', 'GATEWAY-SERVICE') {
    Check "$servicio registrado" ($nombres -contains $servicio) ("-> " + ($nombres -join ', '))
}

Write-Host ''
Write-Host '============================================================'
Write-Host ' 2. Gateway y rutas publicadas'
Write-Host '============================================================'

$salud = Invoke-Api -Metodo GET -Url "$gateway/actuator/health"
Check 'Gateway health UP' ($salud.Status -eq 200 -and $salud.Json.status -eq 'UP')

$rutas = Invoke-Api -Metodo GET -Url "$gateway/actuator/gateway/routes"
Check 'Endpoint de rutas responde 200' ($rutas.Status -eq 200)
$idsRutas = @($rutas.Json | ForEach-Object { $_.route_id })
foreach ($ruta in 'categoria-service', 'producto-service', 'cliente-service', 'pedido-service') {
    Check "Ruta $ruta publicada" ($idsRutas -contains $ruta) ("-> " + ($idsRutas -join ', '))
}

Write-Host ''
Write-Host '============================================================'
Write-Host ' 3. CRUD a traves del Gateway (puerto 8080)'
Write-Host '============================================================'

$listadoCategorias = Invoke-Api -Metodo GET -Url "$gateway/api/categorias"
Check 'GET /api/categorias' ($listadoCategorias.Status -eq 200)

$categoria = Invoke-Api -Metodo POST -Url "$gateway/api/categorias" -Cuerpo @{
    nombre = 'Tecnologia'
    estado = 'ACTIVO'
}
Check 'POST /api/categorias' ($categoria.Status -eq 201) ("-> " + $categoria.Status)
$categoriaId = $categoria.Json.id

$dni = '{0:D8}' -f (Get-Random -Minimum 10000000 -Maximum 99999999)
$cliente = Invoke-Api -Metodo POST -Url "$gateway/api/clientes" -Cuerpo @{
    nombre      = 'Carlos'
    apellidos   = 'Ramirez Torres'
    direccion   = 'Av. Javier Prado 123, Lima'
    telefono    = '987654321'
    nroDni      = $dni
    estado      = 'ACTIVO'
}
Check 'POST /api/clientes' ($cliente.Status -eq 201) ("-> DNI " + $dni)
$clienteId = $cliente.Json.id

$producto = Invoke-Api -Metodo POST -Url "$gateway/api/productos" -Cuerpo @{
    nombre      = 'Laptop Lenovo ThinkPad'
    precio      = 3500.00
    stock       = 15
    categoriaId = $categoriaId
}
Check 'POST /api/productos' ($producto.Status -eq 201)
Check 'Producto enriquecido con categoria via Feign' ($producto.Json.categoria.id -eq $categoriaId) ("-> categoriaId " + $producto.Json.categoria.id)
$productoId = $producto.Json.id

$pedido = Invoke-Api -Metodo POST -Url "$gateway/api/pedidos" -Cuerpo @{
    clienteId  = $clienteId
    productoId = $productoId
    cantidad   = 2
}
Check 'POST /api/pedidos' ($pedido.Status -eq 201)
Check 'Pedido enriquecido con cliente' ($pedido.Json.cliente.id -eq $clienteId)
Check 'Pedido enriquecido con producto' ($pedido.Json.producto.id -eq $productoId)
Check 'Subtotal calculado' ($pedido.Json.subtotal -eq 7000) ("-> " + $pedido.Json.subtotal)
Check 'Estado inicial PENDIENTE' ($pedido.Json.estado -eq 'PENDIENTE')
$pedidoId = $pedido.Json.id

$consulta = Invoke-Api -Metodo GET -Url "$gateway/api/pedidos/$pedidoId"
Check 'GET /api/pedidos/{id}' ($consulta.Status -eq 200 -and $consulta.Json.cliente.id -eq $clienteId)

$atendido = Invoke-Api -Metodo PATCH -Url "$gateway/api/pedidos/$pedidoId/estado" -Cuerpo @{ estado = 'ATENDIDO' }
Check 'PATCH estado a ATENDIDO' ($atendido.Status -eq 200 -and $atendido.Json.estado -eq 'ATENDIDO')

$anulado = Invoke-Api -Metodo PATCH -Url "$gateway/api/pedidos/$pedidoId/estado" -Cuerpo @{ estado = 'ANULADO' }
Check 'PATCH estado a ANULADO' ($anulado.Status -eq 200 -and $anulado.Json.estado -eq 'ANULADO')

$transicion = Invoke-Api -Metodo PATCH -Url "$gateway/api/pedidos/$pedidoId/estado" -Cuerpo @{ estado = 'ATENDIDO' }
Check 'Transicion ANULADO -> ATENDIDO rechazada' ($transicion.Status -eq 400) ("-> " + $transicion.Status)

Write-Host ''
Write-Host '============================================================'
Write-Host ' 4. Casos de error a traves del Gateway'
Write-Host '============================================================'

$cantidadCero = Invoke-Api -Metodo POST -Url "$gateway/api/pedidos" -Cuerpo @{
    clienteId  = $clienteId
    productoId = $productoId
    cantidad   = 0
}
Check 'Pedido con cantidad 0 devuelve 400' ($cantidadCero.Status -eq 400) ("-> " + $cantidadCero.Status)

$clienteInexistente = Invoke-Api -Metodo POST -Url "$gateway/api/pedidos" -Cuerpo @{
    clienteId  = 99999
    productoId = $productoId
    cantidad   = 1
}
Check 'Pedido con cliente inexistente devuelve 404' ($clienteInexistente.Status -eq 404) ("-> " + $clienteInexistente.Status)

$dniInvalido = Invoke-Api -Metodo POST -Url "$gateway/api/clientes" -Cuerpo @{
    nombre    = 'Ana'
    apellidos = 'Perez'
    direccion = 'Calle 1'
    telefono  = '999999999'
    nroDni    = '1234567'
    estado    = 'ACTIVO'
}
Check 'Cliente con DNI de 7 digitos devuelve 400' ($dniInvalido.Status -eq 400) ("-> " + $dniInvalido.Status)

Write-Host ''
Write-Host '============================================================'
Write-Host ' 5. Descubrimiento por nombre logico (locator)'
Write-Host '============================================================'

$porNombre = Invoke-Api -Metodo GET -Url "$gateway/cliente-service/api/clientes"
Check 'GET /cliente-service/api/clientes' ($porNombre.Status -eq 200)

$porNombrePedido = Invoke-Api -Metodo GET -Url "$gateway/pedido-service/api/pedidos"
Check 'GET /pedido-service/api/pedidos' ($porNombrePedido.Status -eq 200)

Write-Host ''
Write-Host '============================================================'
Write-Host (" Resultado: {0} comprobaciones exitosas, {1} fallidas" -f $script:ok, $script:fail)
Write-Host '============================================================'

if ($script:fail -gt 0) { exit 1 }
