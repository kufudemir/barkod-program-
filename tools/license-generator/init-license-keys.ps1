param()

$ErrorActionPreference = "Stop"

function Convert-ToBase64Url([byte[]]$bytes) {
    return [Convert]::ToBase64String($bytes).TrimEnd('=').Replace('+', '-').Replace('/', '_')
}

function Read-EcBlob([byte[]]$blob) {
    $len = [BitConverter]::ToInt32($blob, 4)
    $offset = 8
    $x = $blob[$offset..($offset + $len - 1)]
    $offset += $len
    $y = $blob[$offset..($offset + $len - 1)]
    $offset += $len
    $d = $blob[$offset..($offset + $len - 1)]
    return @{ x = $x; y = $y; d = $d }
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Resolve-Path (Join-Path $scriptDir "..\\..")
$assetDir = Join-Path $projectRoot "app\\src\\main\\assets"
$publicKeyPath = Join-Path $assetDir "premium_public_key.json"
$toolPublicKeyPath = Join-Path $scriptDir "public_key.json"
$privateStoreDir = Join-Path $env:USERPROFILE ".marketpos-license"
$rsaPrivateKeyPath = Join-Path $privateStoreDir "private_key.xml"
$ecPrivateKeyPath = Join-Path $privateStoreDir "ec_private_key.json"

New-Item -ItemType Directory -Force -Path $assetDir | Out-Null
New-Item -ItemType Directory -Force -Path $privateStoreDir | Out-Null

if (-not (Test-Path $rsaPrivateKeyPath)) {
    $rsa = New-Object System.Security.Cryptography.RSACryptoServiceProvider(2048)
    $rsaParams = $rsa.ExportParameters($false)
    $privateXml = $rsa.ToXmlString($true)
    [System.IO.File]::WriteAllText($rsaPrivateKeyPath, $privateXml)
    $rsaPublic = @{
        modulus = (Convert-ToBase64Url $rsaParams.Modulus)
        exponent = (Convert-ToBase64Url $rsaParams.Exponent)
    }
} else {
    $rsa = New-Object System.Security.Cryptography.RSACryptoServiceProvider
    $rsa.FromXmlString((Get-Content $rsaPrivateKeyPath -Raw).Trim())
    $rsaParams = $rsa.ExportParameters($false)
    $rsaPublic = @{
        modulus = (Convert-ToBase64Url $rsaParams.Modulus)
        exponent = (Convert-ToBase64Url $rsaParams.Exponent)
    }
}

if (-not (Test-Path $ecPrivateKeyPath)) {
    $creation = New-Object System.Security.Cryptography.CngKeyCreationParameters
    $creation.ExportPolicy = [System.Security.Cryptography.CngExportPolicies]::AllowExport -bor [System.Security.Cryptography.CngExportPolicies]::AllowPlaintextExport
    $ecKey = [System.Security.Cryptography.CngKey]::Create([System.Security.Cryptography.CngAlgorithm]::ECDsaP256, $null, $creation)
    $privateBlob = $ecKey.Export([System.Security.Cryptography.CngKeyBlobFormat]::EccPrivateBlob)
    $parsed = Read-EcBlob $privateBlob
    $ecPrivate = @{
        curve = "secp256r1"
        x = (Convert-ToBase64Url $parsed.x)
        y = (Convert-ToBase64Url $parsed.y)
        d = (Convert-ToBase64Url $parsed.d)
    }
    [System.IO.File]::WriteAllText($ecPrivateKeyPath, ($ecPrivate | ConvertTo-Json -Compress))
} else {
    $ecPrivate = Get-Content $ecPrivateKeyPath -Raw | ConvertFrom-Json
}

$publicJson = @{
    rsa = $rsaPublic
    ecdsa = @{
        curve = "secp256r1"
        x = $ecPrivate.x
        y = $ecPrivate.y
    }
} | ConvertTo-Json -Compress

[System.IO.File]::WriteAllText($publicKeyPath, $publicJson)
[System.IO.File]::WriteAllText($toolPublicKeyPath, $publicJson)

Write-Output "RSA private key  : $rsaPrivateKeyPath"
Write-Output "ECDSA private key: $ecPrivateKeyPath"
Write-Output "Public key file  : $publicKeyPath"
