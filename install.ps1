# qBoot Installation Script for Windows PowerShell
# This script downloads and installs the latest version of qBoot

param(
    [switch]$Force,
    [switch]$Help,
    [string]$InstallPath = "$env:LOCALAPPDATA\qboot"
)

# Configuration
$QBootRepo = "falkzilm/qboot"
$TempDir = "$env:TEMP\qboot-install"

# Function to write colored output
function Write-ColoredOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

# Function to print banner
function Show-Banner {
    Write-Host ""
    Write-ColoredOutput "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓" -Color Cyan
    Write-ColoredOutput "┃  🚀 qBoot Installer for Windows                      ┃" -Color Cyan  
    Write-ColoredOutput "┃     Bootstrap projects from templates - CLI tool     ┃" -Color Cyan
    Write-ColoredOutput "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛" -Color Cyan
    Write-Host ""
}

# Function to show help
function Show-Help {
    Write-Host "qBoot Installation Script for Windows"
    Write-Host ""
    Write-Host "Usage: .\install.ps1 [OPTIONS]"
    Write-Host ""
    Write-Host "Options:"
    Write-Host "  -Force          Force installation even if qBoot is already installed"
    Write-Host "  -Help           Show this help message"  
    Write-Host "  -InstallPath    Custom installation directory (default: %LOCALAPPDATA%\qboot)"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\install.ps1                                  # Standard installation"
    Write-Host "  .\install.ps1 -Force                          # Force reinstall"
    Write-Host "  .\install.ps1 -InstallPath C:\Tools\qboot     # Custom install path"
    Write-Host ""
}

# Function to check prerequisites
function Test-Prerequisites {
    Write-ColoredOutput "🔍 Checking prerequisites..." -Color Blue
    
    # Check PowerShell version
    if ($PSVersionTable.PSVersion.Major -lt 5) {
        Write-ColoredOutput "❌ PowerShell 5.0 or higher is required" -Color Red
        Write-ColoredOutput "Please upgrade PowerShell and try again" -Color Yellow
        exit 1
    }
    
    Write-ColoredOutput "✅ PowerShell version: $($PSVersionTable.PSVersion)" -Color Green
    
    # Check if running as Administrator (optional but recommended)
    $currentPrincipal = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
    $isAdmin = $currentPrincipal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
    
    if ($isAdmin) {
        Write-ColoredOutput "✅ Running as Administrator" -Color Green
    } else {
        Write-ColoredOutput "⚠️  Not running as Administrator (PATH update may require manual action)" -Color Yellow
    }
    
    # Test internet connectivity
    try {
        $null = Invoke-WebRequest -Uri "https://api.github.com" -UseBasicParsing -TimeoutSec 10
        Write-ColoredOutput "✅ Internet connectivity verified" -Color Green
    } catch {
        Write-ColoredOutput "❌ No internet connection or GitHub is not accessible" -Color Red
        Write-ColoredOutput "Please check your internet connection and try again" -Color Yellow
        exit 1
    }
}

# Function to get latest release info
function Get-LatestRelease {
    Write-ColoredOutput "🔍 Checking for latest release..." -Color Blue
    
    $apiUrl = "https://api.github.com/repos/$QBootRepo/releases/latest"
    
    try {
        $response = Invoke-RestMethod -Uri $apiUrl -UseBasicParsing
        $script:Version = $response.tag_name
        $script:DownloadUrl = "https://github.com/$QBootRepo/releases/latest/download/qboot-windows.exe"
        
        Write-ColoredOutput "✅ Found version: $script:Version" -Color Green
    } catch {
        Write-ColoredOutput "❌ Failed to fetch release information" -Color Red
        Write-ColoredOutput "Falling back to direct binary download..." -Color Yellow
        $script:Version = "latest"
        $script:DownloadUrl = "https://github.com/$QBootRepo/releases/latest/download/qboot-windows.exe"
    }
}

# Function to download binary
function Get-QBootBinary {
    Write-ColoredOutput "📥 Downloading qBoot for Windows..." -Color Blue
    
    # Create temporary directory
    if (Test-Path $TempDir) {
        Remove-Item $TempDir -Recurse -Force
    }
    New-Item -ItemType Directory -Path $TempDir -Force | Out-Null
    
    $binaryPath = Join-Path $TempDir "qboot.exe"
    
    try {
        # Use Invoke-WebRequest to download the binary
        Invoke-WebRequest -Uri $script:DownloadUrl -OutFile $binaryPath -UseBasicParsing
        Write-ColoredOutput "✅ Downloaded successfully" -Color Green
    } catch {
        Write-ColoredOutput "❌ Failed to download qBoot binary" -Color Red
        Write-ColoredOutput "Error: $($_.Exception.Message)" -Color Yellow
        Write-ColoredOutput "Please check your internet connection and try again" -Color Yellow
        exit 1
    }
    
    # Verify the file was downloaded and has content
    if (-not (Test-Path $binaryPath) -or (Get-Item $binaryPath).Length -eq 0) {
        Write-ColoredOutput "❌ Downloaded file is invalid or empty" -Color Red
        exit 1
    }
    
    $script:BinaryPath = $binaryPath
}

# Function to install binary
function Install-QBoot {
    Write-ColoredOutput "📦 Installing qBoot..." -Color Blue
    
    # Create install directory if it doesn't exist
    if (-not (Test-Path $InstallPath)) {
        New-Item -ItemType Directory -Path $InstallPath -Force | Out-Null
        Write-ColoredOutput "Created installation directory: $InstallPath" -Color Blue
    }
    
    $targetPath = Join-Path $InstallPath "qboot.exe"
    
    # Copy binary to install directory
    try {
        Copy-Item $script:BinaryPath $targetPath -Force
        Write-ColoredOutput "✅ Installed to $targetPath" -Color Green
    } catch {
        Write-ColoredOutput "❌ Failed to install qBoot" -Color Red
        Write-ColoredOutput "Error: $($_.Exception.Message)" -Color Yellow
        exit 1
    }
    
    $script:InstalledPath = $targetPath
}

# Function to update PATH
function Update-PathEnvironment {
    Write-ColoredOutput "🔧 Checking PATH configuration..." -Color Blue
    
    # Get current PATH
    $currentPath = [Environment]::GetEnvironmentVariable("PATH", [EnvironmentVariableTarget]::User)
    
    # Check if install directory is already in PATH
    if ($currentPath -like "*$InstallPath*") {
        Write-ColoredOutput "✅ $InstallPath is already in PATH" -Color Green
        return
    }
    
    Write-ColoredOutput "⚠️  $InstallPath is not in your PATH" -Color Yellow
    Write-ColoredOutput "📝 Adding $InstallPath to user PATH..." -Color Blue
    
    try {
        # Add to user PATH
        $newPath = if ($currentPath) { "$currentPath;$InstallPath" } else { $InstallPath }
        [Environment]::SetEnvironmentVariable("PATH", $newPath, [EnvironmentVariableTarget]::User)
        
        Write-ColoredOutput "✅ Updated user PATH" -Color Green
        Write-ColoredOutput "⚠️  Please restart your command prompt or PowerShell session" -Color Yellow
    } catch {
        Write-ColoredOutput "❌ Failed to update PATH automatically" -Color Red
        Write-ColoredOutput "Please manually add $InstallPath to your PATH" -Color Yellow
    }
}

# Function to test installation
function Test-Installation {
    Write-ColoredOutput "🧪 Testing installation..." -Color Blue
    
    try {
        $output = & $script:InstalledPath --version 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-ColoredOutput "✅ qBoot is working correctly" -Color Green
        } else {
            Write-ColoredOutput "❌ qBoot installation test failed" -Color Red
            Write-ColoredOutput "Output: $output" -Color Yellow
            exit 1
        }
    } catch {
        Write-ColoredOutput "❌ qBoot installation test failed" -Color Red
        Write-ColoredOutput "Error: $($_.Exception.Message)" -Color Yellow
        exit 1
    }
}

# Function to cleanup
function Remove-TempFiles {
    Write-ColoredOutput "🧹 Cleaning up..." -Color Blue
    
    try {
        if (Test-Path $TempDir) {
            Remove-Item $TempDir -Recurse -Force
        }
        Write-ColoredOutput "✅ Cleanup completed" -Color Green
    } catch {
        Write-ColoredOutput "⚠️  Could not clean up temporary files" -Color Yellow
    }
}

# Function to show success message
function Show-Success {
    Write-Host ""
    Write-ColoredOutput "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -Color Green
    Write-ColoredOutput "🎉 qBoot installed successfully!" -Color Green
    Write-ColoredOutput "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -Color Green
    Write-Host ""
    Write-ColoredOutput "📍 Installation location: $script:InstalledPath" -Color Cyan
    Write-ColoredOutput "📋 Version: $script:Version" -Color Cyan
    Write-Host ""
    Write-ColoredOutput "🚀 Quick start:" -Color Blue
    Write-ColoredOutput "  qboot --help                    # Show help" -Color Yellow
    Write-ColoredOutput "  qboot --list                    # List frameworks" -Color Yellow  
    Write-ColoredOutput "  qboot create -t template.xml    # Create project" -Color Yellow
    Write-Host ""
    Write-ColoredOutput "📖 Documentation:" -Color Blue
    Write-ColoredOutput "  https://github.com/$QBootRepo" -Color Cyan
    Write-Host ""
    
    # Check if PATH needs attention
    $currentPath = [Environment]::GetEnvironmentVariable("PATH", [EnvironmentVariableTarget]::User)
    if ($currentPath -notlike "*$InstallPath*") {
        Write-ColoredOutput "⚠️  Don't forget to restart your command prompt or PowerShell!" -Color Yellow
        Write-ColoredOutput "   Or run: `$env:PATH += `;$InstallPath` for this session only" -Color Yellow
        Write-Host ""
    }
}

# Main installation function
function Install-Main {
    # Handle help parameter
    if ($Help) {
        Show-Help
        return
    }
    
    Show-Banner
    
    # Check if qBoot is already installed and not forcing
    if (-not $Force) {
        $existingQBoot = Get-Command qboot -ErrorAction SilentlyContinue
        if ($existingQBoot) {
            Write-ColoredOutput "⚠️  qBoot is already installed" -Color Yellow
            try {
                $currentVersion = & qboot --version 2>&1
                Write-ColoredOutput "Current version: $currentVersion" -Color Cyan
            } catch {
                Write-ColoredOutput "Current version: unknown" -Color Cyan
            }
            Write-ColoredOutput "Use -Force to reinstall" -Color Yellow
            return
        }
    }
    
    try {
        Test-Prerequisites
        Get-LatestRelease
        Get-QBootBinary
        Install-QBoot
        Update-PathEnvironment
        Test-Installation
        Show-Success
    } catch {
        Write-ColoredOutput "❌ Installation failed: $($_.Exception.Message)" -Color Red
        exit 1
    } finally {
        Remove-TempFiles
    }
}

# Handle Ctrl+C gracefully
$null = Register-EngineEvent -SourceIdentifier "PowerShell.Exiting" -Action {
    Write-Host ""
    Write-ColoredOutput "❌ Installation cancelled" -Color Red
    Remove-TempFiles
}

# Run main installation
Install-Main

Write-ColoredOutput "Happy bootstrapping! 🚀" -Color Green