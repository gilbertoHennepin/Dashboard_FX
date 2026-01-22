# PowerShell script to run the JavaFX application
# This script attempts to run the application using  Java

# to run on Cursor use command below in terminal
# .\run.ps1

Write-Host "Attempting to run Dashboard FX..." -ForegroundColor Green

# Check if Java is available
$javaCmd = Get-Command java -ErrorAction SilentlyContinue
if (-not $javaCmd) {
    Write-Host "ERROR: Java not found in PATH" -ForegroundColor Red
    Write-Host "Please install a JDK or run from IntelliJ IDEA" -ForegroundColor Yellow
    exit 1
}

# Check if Maven wrapper exists
if (Test-Path ".\mvnw.cmd") {
    Write-Host "Using Maven wrapper..." -ForegroundColor Cyan
    
    # Try to find JDK for Maven
    $jdkPaths = @(
        "$env:JAVA_HOME",
        "C:\Program Files\JetBrains\IntelliJ IDEA *\jbr",
        "C:\Program Files\Java\jdk-17",
        "C:\Program Files\Java\jdk-21",
        "C:\Program Files\Eclipse Adoptium\jdk-17*",
        "C:\Program Files\Eclipse Adoptium\jdk-21*",
        "$env:LOCALAPPDATA\Programs\Eclipse Adoptium\jdk-17*",
        "$env:LOCALAPPDATA\Programs\Eclipse Adoptium\jdk-21*"
    )
    
    $foundJdk = $null
    foreach ($path in $jdkPaths) {
        if ($path -and (Test-Path $path)) {
            $jdkPath = if ($path -like "*\*") { 
                Get-ChildItem $path -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty FullName
            } else { $path }
            
            if ($jdkPath -and (Test-Path "$jdkPath\bin\javac.exe")) {
                $foundJdk = $jdkPath
                Write-Host "Found JDK at: $foundJdk" -ForegroundColor Green
                break
            }
        }
    }
    
    if ($foundJdk) {
        $env:JAVA_HOME = $foundJdk
        Write-Host "Set JAVA_HOME to: $env:JAVA_HOME" -ForegroundColor Cyan
        .\mvnw.cmd javafx:run
    } else {
        Write-Host "ERROR: No JDK found. Maven requires a JDK (not just JRE)." -ForegroundColor Red
        Write-Host ""
        Write-Host "Options:" -ForegroundColor Yellow
        Write-Host "1. Install JDK 17 or 21 from: https://adoptium.net/" -ForegroundColor White
        Write-Host "2. Run from IntelliJ IDEA (right-click HelloApplication.java -> Run)" -ForegroundColor White
        Write-Host "3. Set JAVA_HOME manually: `$env:JAVA_HOME = 'C:\path\to\jdk'" -ForegroundColor White
        exit 1
    }
} else {
    Write-Host "ERROR: Maven wrapper (mvnw.cmd) not found" -ForegroundColor Red
    exit 1
}
