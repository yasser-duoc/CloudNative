# Despliegue de DigitalFix en AWS EC2 (un solo comando).
# Uso:
#   .\deploy-aws.ps1 -TenantId "<TENANT_ID>" -ApiClientId "<API_CLIENT_ID>"
#
# Requisitos: `aws sts get-caller-identity` debe funcionar (sesion valida, region us-east-1).
# Costos aproximados (us-east-1, solo mientras la instancia corre):
#   t3.xlarge (16GB, por defecto): ~US$0.166/hora
#   IMPORTANTE: al terminar la defensa, TERMINAR la instancia (stop-ec2 mas abajo),
#   porque una instancia detenida sigue cobrando el disco EBS (~US$0.08/GB-mes).

param(
    [Parameter(Mandatory = $true)][string]$TenantId,
    [Parameter(Mandatory = $true)][string]$ApiClientId,
    [string]$InstanceType = "t3.xlarge",
    [string]$Region = "us-east-1"
)

$ErrorActionPreference = "Stop"
$Key  = "digitalfix-key"
$Sg   = "digitalfix-sg"
$Ami  = "resolve:ssm:/aws/service/canonical/ubuntu/server/22.04/stable/current/amd64/hvm/ebs-gp2/ami-id"

Write-Host "== 1/5 Validando sesion AWS..."
aws sts get-caller-identity --region $Region | Out-Null

Write-Host "== 2/5 Key pair + Security group..."
if (-not (Test-Path "$PSScriptRoot\digitalfix-key.pem")) {
    aws ec2 create-key-pair --region $Region --key-name $Key --query "KeyMaterial" --output text | Out-File -Encoding ascii "$PSScriptRoot\digitalfix-key.pem"
}
$sgId = aws ec2 describe-security-groups --region $Region --group-names $Sg --query "SecurityGroups[0].GroupId" --output text 2>$null
if (-not $sgId) {
    $sgId = aws ec2 create-security-group --region $Region --group-name $Sg --description "DigitalFix demo" --query "GroupId" --output text
    foreach ($port in @(22, 3000, 8080, 8081, 8082, 8083, 8084, 8085)) {
        aws ec2 authorize-security-group-ingress --region $Region --group-id $sgId --protocol tcp --port $port --cidr 0.0.0.0/0 | Out-Null
    }
}

Write-Host "== 3/5 Lanzando instancia $InstanceType (esto no se repite)..."
$UserData = Get-Content "$PSScriptRoot\user-data.sh" -Raw
$UserDataB64 = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($UserData))
$InstanceId = aws ec2 run-instances --region $Region `
    --image-id $Ami --instance-type $InstanceType --key-name $Key --security-group-ids $sgId `
    --user-data $UserDataB64 --count 1 `
    --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=digitalfix-demo}]" `
    --query "Instances[0].InstanceId" --output text

Write-Host "== 4/5 Esperando IP publica de $InstanceId..."
$Ip = $null
while (-not $Ip) { Start-Sleep 10
    $Ip = aws ec2 describe-instances --region $Region --instance-ids $InstanceId --query "Reservations[0].Instances[0].PublicIpAddress" --output text }
Write-Host "IP: $Ip"

Write-Host "== 5/5 Listo. El stack se construye dentro de la instancia (10-20 min)."
Write-Host "   Frontend:      http://$Ip`:3000"
Write-Host "   BFF health:    http://$Ip`:8080/actuator/health"
Write-Host "   Ver progreso:  ssh -i $PSScriptRoot\digitalfix-key.pem ubuntu@$Ip  luego:  tail -f /var/log/cloud-init-output.log"
Write-Host ""
Write-Host "   >>> AL TERMINAR LA DEMO (para no gastar credito) <<<"
Write-Host "   aws ec2 terminate-instances --region $Region --instance-ids $InstanceId"
