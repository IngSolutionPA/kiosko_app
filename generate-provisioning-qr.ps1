param(
    [string]$JsonPath = "C:\Users\lvillarreal\AndroidStudioProjects\kioskoPDA\provisioning-qr-template.json",
    [string]$OutputPng = "C:\Users\lvillarreal\AndroidStudioProjects\kioskoPDA\provisioning-qr.png"
)

if (-not (Test-Path $JsonPath)) {
    throw "No existe el archivo JSON: $JsonPath"
}

Add-Type -AssemblyName System.Web
$json = Get-Content $JsonPath -Raw

try {
    $payload = $json | ConvertFrom-Json
} catch {
    throw "El JSON de aprovisionamiento no es válido. Revisa $JsonPath. Detalle: $($_.Exception.Message)"
}

$compactJson = $payload | ConvertTo-Json -Compress
$encoded = [System.Web.HttpUtility]::UrlEncode($compactJson)
$url = "https://quickchart.io/qr?size=700&text=$encoded"

Invoke-WebRequest -Uri $url -OutFile $OutputPng
Write-Host "QR generado en: $OutputPng"

