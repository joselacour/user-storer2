$ErrorActionPreference = "Stop"

$TableName = "User"
$Region = "us-east-1"
$Endpoint = "http://localhost:4566"

$ScriptDir = $PSScriptRoot
$TableInitFile = Join-Path $ScriptDir "table-init.json"
$UserDataFile = Join-Path $ScriptDir "user-data.json"

$Secret1Name = "jwt-private-key"
$Secret2Name = "jwt-public-key"
$PrivateKeyFile = Join-Path $ScriptDir "private.pem"
$PublicKeyFile = Join-Path $ScriptDir "public.pem"

Write-Host "Initializing DynamoDB table, generating keys and Secrets Manager secrets..."

# Creamos la tabla si no existe, la poblamos y esperamos a que este activa
function existTable {
    try {
        aws dynamodb describe-table --table-name $TableName --endpoint-url $Endpoint --region $Region --query "Table.TableStatus" --output text 2>$null | Out-Null
        return $true
    } catch {
        return $false
    }
}

if (-not (existTable)) {
    aws dynamodb create-table --cli-input-json file://$TableInitFile --endpoint-url $Endpoint --region $Region | Out-Null

    do {
        Start-Sleep -Seconds 1
        $status = aws dynamodb describe-table --table-name $TableName --endpoint-url $Endpoint --region $Region --query "Table.TableStatus" --output text
    } while ($status -ne "ACTIVE")
}

aws dynamodb batch-write-item --request-items file://$UserDataFile --endpoint-url $Endpoint --region $Region | Out-Null
Write-Host "DynamoDB table '$TableName' is initialized."

# Creamos las claves publicas y privadas si no existen
if (-not (Test-Path -Path $PrivateKeyFile -PathType Leaf)) {
    openssl genpkey -algorithm RSA -out $PrivateKeyFile -pkeyopt rsa_keygen_bits:2048 | Out-Null
    Write-Host "Private key generated at '$PrivateKeyFile'."
} else {
    Write-Host "Private key already exists at '$PrivateKeyFile'."
}

if (-not (Test-Path -Path $PublicKeyFile -PathType Leaf)) {
    openssl rsa -pubout -in $PrivateKeyFile -out $PublicKeyFile | Out-Null
    Write-Host "Public key generated at '$PublicKeyFile'."
} else {
    Write-Host "Public key already exists at '$PublicKeyFile'."
}


# Creamos los secretos si no existen, y los poblamos con las claves
function existSecret1 {
    try {
        aws --endpoint-url=$Endpoint secretsmanager get-secret-value --secret-id $Secret1Name --output text 2>$null | Out-Null
        return $true
    } catch {
        return $false
    }
}

if (-not (existSecret1)) {
    aws --endpoint-url=$Endpoint secretsmanager create-secret --name $Secret1Name --secret-string "$(Get-Content -Raw -Path $PrivateKeyFile)"
    Write-Host "Secret '$Secret1Name' created."
} else {
    Write-Host "Secret '$Secret1Name' already exists."

}
function existSecret2 {
    try {
        aws --endpoint-url=$Endpoint secretsmanager get-secret-value --secret-id $Secret2Name --output text 2>$null | Out-Null
        return $true
    } catch {
        return $false
    }
}

if (-not (existSecret2)) {
    aws --endpoint-url=$Endpoint secretsmanager create-secret --name $Secret2Name --secret-string "$(Get-Content -Raw -Path $PublicKeyFile)"
    Write-Host "Secret '$Secret2Name' created."
} else {
    Write-Host "Secret '$Secret2Name' already exists."
}

Write-Host "Finishing initialization of DynamoDB table, keys and secrets."