<#
.SYNOPSIS
  FaturaLab UI/UAT testlerini koşar ve SONUÇ NE OLURSA OLSUN (pass/fail/build failure)
  güncel extended (masterthought) raporu üretip tarayıcıda açar.

.DESCRIPTION
  Kronik sorun: mvn test build FAILURE dönünce TestNG @AfterSuite'in rapor üretimi/açması
  güvenilmez oluyordu ve bir önceki koşumun bayat raporu açılıyordu. Bu wrapper, raporu
  test JVM'inden BAĞIMSIZ olarak (ReportRegen ile) her koşumda taze üretip açar.

.PARAMETER Suite
  uat (varsayılan) | ui

.PARAMETER Tags
  Cucumber tag filtresi, örn: "@tzf-001" veya "@uat and @kritik". Boşsa runner varsayılanı.

.PARAMETER Env
  dev (varsayılan) | dev2

.EXAMPLE
  .\run-tests.ps1
  .\run-tests.ps1 -Suite uat -Tags "@fl-014"
  .\run-tests.ps1 -Suite ui -Tags "@tzf-001" -Env dev
#>
param(
    [ValidateSet("uat", "ui")]
    [string]$Suite = "uat",
    [string]$Tags = "",
    [ValidateSet("dev", "dev2")]
    [string]$Env = "dev"
)

$ErrorActionPreference = "Continue"
Set-Location $PSScriptRoot

if ($Suite -eq "ui") { $runner = "UITestRunner" } else { $runner = "UATTestRunner" }

Write-Host "==> Test koşumu: suite=$Suite runner=$runner env=$Env tags='$Tags'" -ForegroundColor Cyan

# Raporu wrapper açacağı için mvn'e open.reports=false ver (çift açılışı önle).
$mvnArgs = @("test", "-Dtest=$runner", "-Denv=$Env", "-Dfaturalab.open.reports=false")
if ($Tags -ne "") { $mvnArgs += "-Dcucumber.filter.tags=$Tags" }

& mvn @mvnArgs
$testExit = $LASTEXITCODE
Write-Host "==> mvn bitti (exit=$testExit). Rapor test sonucundan BAĞIMSIZ üretilecek." -ForegroundColor Cyan

# Bağımlılık classpath'i (bir kez üret, sonra cache'le).
if (-not (Test-Path "target/cp.txt")) {
    Write-Host "==> Classpath üretiliyor (target/cp.txt)..." -ForegroundColor DarkGray
    & mvn -q dependency:build-classpath "-Dmdep.outputFile=target/cp.txt" | Out-Null
}
$cp = "target/classes;target/test-classes;" + ((Get-Content "target/cp.txt" -Raw).Trim())

# Güncel raporu KESİN üret + aç (build FAILURE olsa bile).
& java -cp $cp com.faturalab.automation.reporting.ReportRegen $Suite --open

Write-Host "==> Bitti. Test exit kodu korunuyor: $testExit" -ForegroundColor Cyan
exit $testExit
