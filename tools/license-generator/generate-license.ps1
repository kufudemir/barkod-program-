param(
    [Parameter(Mandatory = $true)]
    [string]$DeviceCode,
    [string]$Tier = "PRO",
    [int]$DaysValid = 0
)

$ErrorActionPreference = "Stop"

function Convert-ToBase64Url([byte[]]$bytes) {
    return [Convert]::ToBase64String($bytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')
}

function Convert-FromBase64Url([string]$text) {
    $normalized = $text.Replace('-', '+').Replace('_', '/')
    $normalized += "=" * ((4 - $normalized.Length % 4) % 4)
    return [Convert]::FromBase64String($normalized)
}

function Convert-ToBase36([Int64]$value) {
    if ($value -eq 0) { return "0" }
    $chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    $result = ""
    $current = $value
    while ($current -gt 0) {
        $remainder = [int]($current % 36)
        $result = $chars[$remainder] + $result
        $current = [math]::Floor($current / 36)
    }
    return $result
}

function New-EcPrivateBlob([byte[]]$x, [byte[]]$y, [byte[]]$d) {
    $blob = New-Object byte[] (8 + $x.Length + $y.Length + $d.Length)
    [System.Text.Encoding]::ASCII.GetBytes("ECS2").CopyTo($blob, 0)
    [BitConverter]::GetBytes($x.Length).CopyTo($blob, 4)
    $x.CopyTo($blob, 8)
    $y.CopyTo($blob, 8 + $x.Length)
    $d.CopyTo($blob, 8 + $x.Length + $y.Length)
    return $blob
}

$privateStoreDir = Join-Path $env:USERPROFILE ".marketpos-license"
$ecPrivateKeyPath = Join-Path $privateStoreDir "ec_private_key.json"
$rsaPrivateKeyPath = Join-Path $privateStoreDir "private_key.xml"

$issuedAt = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$expiresAt = $null
if ($DaysValid -gt 0) {
    $expiresAt = [DateTimeOffset]::UtcNow.AddDays($DaysValid).ToUnixTimeMilliseconds()
}

$tierCode = switch ($Tier.ToUpperInvariant()) {
    "PRO" { "P" }
    default { throw "Only PRO tier is supported in generator" }
}

$normalizedDeviceCode = $DeviceCode.Trim().ToUpperInvariant().Replace("-", "")
$nonceBytes = New-Object byte[] 8
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($nonceBytes)
$nonce = Convert-ToBase64Url $nonceBytes
$expiresText = if ($expiresAt) { Convert-ToBase36 $expiresAt } else { "-" }
$payloadText = "$tierCode|$normalizedDeviceCode|$(Convert-ToBase36 $issuedAt)|$expiresText|$nonce"
$payloadBytes = [System.Text.Encoding]::UTF8.GetBytes($payloadText)

$algorithmUsed = ""
if (Test-Path $ecPrivateKeyPath) {
    $ecKeyJson = Get-Content $ecPrivateKeyPath -Raw | ConvertFrom-Json
    $x = Convert-FromBase64Url $ecKeyJson.x
    $y = Convert-FromBase64Url $ecKeyJson.y
    $d = Convert-FromBase64Url $ecKeyJson.d
    $blob = New-EcPrivateBlob $x $y $d
    $key = [System.Security.Cryptography.CngKey]::Import($blob, [System.Security.Cryptography.CngKeyBlobFormat]::EccPrivateBlob)
    $ecdsa = New-Object System.Security.Cryptography.ECDsaCng($key)
    $signatureBytes = $ecdsa.SignData($payloadBytes, [System.Security.Cryptography.HashAlgorithmName]::SHA256)
    $algorithmUsed = "ECDSA-P256"
} elseif (Test-Path $rsaPrivateKeyPath) {
    $privateXml = (Get-Content $rsaPrivateKeyPath -Raw).Trim()
    $rsa = New-Object System.Security.Cryptography.RSACryptoServiceProvider
    $rsa.FromXmlString($privateXml)
    $signatureBytes = $rsa.SignData(
        $payloadBytes,
        [System.Security.Cryptography.HashAlgorithmName]::SHA256,
        [System.Security.Cryptography.RSASignaturePadding]::Pkcs1
    )
    $algorithmUsed = "RSA-2048"
} else {
    throw "Private key not found. First run tools\\license-generator\\init-license-keys.ps1"
}

$licenseCode = "$(Convert-ToBase64Url $payloadBytes).$(Convert-ToBase64Url $signatureBytes)"

Write-Output "Device Code : $DeviceCode"
Write-Output "Tier        : $Tier"
Write-Output "Issued At   : $issuedAt"
Write-Output "Expires At  : $expiresAt"
Write-Output "Algorithm   : $algorithmUsed"
Write-Output "Payload Len : $($payloadText.Length)"
Write-Output ""
Write-Output $licenseCode
