#!/bin/bash

# qBoot Installation Script for Unix/Linux/macOS
# This script downloads and installs the latest version of qBoot

set -e

# Configuration
QBOOT_REPO="falkzilm/qboot"
INSTALL_DIR="${HOME}/.local/bin"
TEMP_DIR="/tmp/qboot-install"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Detect OS and architecture
detect_platform() {
    local os arch
    os="$(uname -s)"
    arch="$(uname -m)"
    
    case "$os" in
        Darwin) os="macos" ;;
        Linux) os="linux" ;;
        *) 
            echo -e "${RED}❌ Unsupported operating system: $os${NC}"
            exit 1
        ;;
    esac
    
    case "$arch" in
        x86_64|amd64) arch="x86_64" ;;
        arm64|aarch64) arch="arm64" ;;
        *)
            echo -e "${RED}❌ Unsupported architecture: $arch${NC}"
            exit 1
        ;;
    esac
    
    PLATFORM="$os"
    ARCH="$arch"
}

# Print banner
print_banner() {
    echo -e "${CYAN}"
    echo "┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓"
    echo "┃  🚀 qBoot Installer                                  ┃"
    echo "┃     Bootstrap projects from templates - CLI tool     ┃"
    echo "┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛"
    echo -e "${NC}"
}

# Check prerequisites
check_prerequisites() {
    echo -e "${BLUE}🔍 Checking prerequisites...${NC}"
    
    # Check for curl or wget
    if command -v curl >/dev/null 2>&1; then
        DOWNLOADER="curl"
    elif command -v wget >/dev/null 2>&1; then
        DOWNLOADER="wget"
    else
        echo -e "${RED}❌ curl or wget is required but not installed${NC}"
        echo -e "${YELLOW}Please install curl or wget and try again${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Found $DOWNLOADER${NC}"
    
    # Check for unzip or tar
    if command -v unzip >/dev/null 2>&1; then
        EXTRACTOR="unzip"
    elif command -v tar >/dev/null 2>&1; then
        EXTRACTOR="tar"
    else
        echo -e "${RED}❌ unzip or tar is required but not installed${NC}"
        echo -e "${YELLOW}Please install unzip or tar and try again${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Found $EXTRACTOR${NC}"
}

# Get latest release info from GitHub
get_latest_release() {
    echo -e "${BLUE}🔍 Checking for latest release...${NC}"
    
    local api_url="https://api.github.com/repos/$QBOOT_REPO/releases/latest"
    local release_info
    
    if [ "$DOWNLOADER" = "curl" ]; then
        release_info=$(curl -s "$api_url" || echo "")
    else
        release_info=$(wget -qO- "$api_url" || echo "")
    fi
    
    if [ -z "$release_info" ]; then
        echo -e "${RED}❌ Failed to fetch release information${NC}"
        echo -e "${YELLOW}Falling back to direct binary download...${NC}"
        DOWNLOAD_URL="https://github.com/$QBOOT_REPO/releases/latest/download/qboot-$PLATFORM"
        VERSION="latest"
        return
    fi
    
    # Extract version and download URL
    VERSION=$(echo "$release_info" | grep '"tag_name"' | sed -E 's/.*"tag_name": "([^"]+)".*/\1/' || echo "latest")
    DOWNLOAD_URL="https://github.com/$QBOOT_REPO/releases/latest/download/qboot-$PLATFORM"
    
    echo -e "${GREEN}✅ Found version: $VERSION${NC}"
}

# Download qBoot binary
download_binary() {
    echo -e "${BLUE}📥 Downloading qBoot for $PLATFORM...${NC}"
    
    # Create temporary directory
    rm -rf "$TEMP_DIR"
    mkdir -p "$TEMP_DIR"
    
    local binary_path="$TEMP_DIR/qboot"
    
    if [ "$DOWNLOADER" = "curl" ]; then
        if ! curl -L -o "$binary_path" "$DOWNLOAD_URL" --fail --show-error; then
            echo -e "${RED}❌ Failed to download qBoot binary${NC}"
            echo -e "${YELLOW}Please check your internet connection and try again${NC}"
            exit 1
        fi
    else
        if ! wget -O "$binary_path" "$DOWNLOAD_URL"; then
            echo -e "${RED}❌ Failed to download qBoot binary${NC}"
            echo -e "${YELLOW}Please check your internet connection and try again${NC}"
            exit 1
        fi
    fi
    
    # Make binary executable
    chmod +x "$binary_path"
    
    echo -e "${GREEN}✅ Downloaded successfully${NC}"
}

# Install binary
install_binary() {
    echo -e "${BLUE}📦 Installing qBoot...${NC}"
    
    # Create install directory if it doesn't exist
    mkdir -p "$INSTALL_DIR"
    
    # Copy binary to install directory
    cp "$TEMP_DIR/qboot" "$INSTALL_DIR/qboot"
    
    echo -e "${GREEN}✅ Installed to $INSTALL_DIR/qboot${NC}"
}

# Update PATH if necessary
update_path() {
    echo -e "${BLUE}🔧 Checking PATH configuration...${NC}"
    
    # Check if install directory is in PATH
    if [[ ":$PATH:" == *":$INSTALL_DIR:"* ]]; then
        echo -e "${GREEN}✅ $INSTALL_DIR is already in PATH${NC}"
        return
    fi
    
    echo -e "${YELLOW}⚠️  $INSTALL_DIR is not in your PATH${NC}"
    
    # Determine shell configuration file
    local shell_config=""
    case "$SHELL" in
        */zsh) shell_config="$HOME/.zshrc" ;;
        */bash) 
            if [[ -f "$HOME/.bashrc" ]]; then
                shell_config="$HOME/.bashrc"
            else
                shell_config="$HOME/.bash_profile"
            fi
        ;;
        */fish) shell_config="$HOME/.config/fish/config.fish" ;;
        *) shell_config="$HOME/.profile" ;;
    esac
    
    echo -e "${BLUE}📝 Adding $INSTALL_DIR to PATH in $shell_config${NC}"
    
    # Add to PATH
    echo "" >> "$shell_config"
    echo "# Added by qBoot installer" >> "$shell_config"
    echo "export PATH=\"\$HOME/.local/bin:\$PATH\"" >> "$shell_config"
    
    echo -e "${GREEN}✅ Updated $shell_config${NC}"
    echo -e "${YELLOW}⚠️  Please restart your shell or run: source $shell_config${NC}"
}

# Test installation
test_installation() {
    echo -e "${BLUE}🧪 Testing installation...${NC}"
    
    # Test if qBoot is executable
    if "$INSTALL_DIR/qboot" --version >/dev/null 2>&1; then
        echo -e "${GREEN}✅ qBoot is working correctly${NC}"
    else
        echo -e "${RED}❌ qBoot installation test failed${NC}"
        exit 1
    fi
}

# Cleanup
cleanup() {
    echo -e "${BLUE}🧹 Cleaning up...${NC}"
    rm -rf "$TEMP_DIR"
    echo -e "${GREEN}✅ Cleanup completed${NC}"
}

# Print success message
print_success() {
    echo -e "${GREEN}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "🎉 qBoot installed successfully!"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo -e "${NC}"
    echo -e "${CYAN}📍 Installation location: $INSTALL_DIR/qboot${NC}"
    echo -e "${CYAN}📋 Version: $VERSION${NC}"
    echo ""
    echo -e "${BLUE}🚀 Quick start:${NC}"
    echo -e "${YELLOW}  qboot --help                    # Show help${NC}"
    echo -e "${YELLOW}  qboot --list                    # List frameworks${NC}"
    echo -e "${YELLOW}  qboot create -t template.xml    # Create project${NC}"
    echo ""
    echo -e "${BLUE}📖 Documentation:${NC}"
    echo -e "${CYAN}  https://github.com/$QBOOT_REPO${NC}"
    echo ""
    
    if [[ ":$PATH:" != *":$INSTALL_DIR:"* ]]; then
        echo -e "${YELLOW}⚠️  Don't forget to restart your shell or run:${NC}"
        echo -e "${YELLOW}   source ~/.bashrc (or your shell's config file)${NC}"
        echo ""
    fi
}

# Main installation function
main() {
    print_banner
    
    # Check if running as root (not recommended)
    if [ "$EUID" -eq 0 ]; then
        echo -e "${YELLOW}⚠️  Running as root is not recommended${NC}"
        echo -e "${YELLOW}   qBoot will be installed system-wide${NC}"
        INSTALL_DIR="/usr/local/bin"
    fi
    
    detect_platform
    check_prerequisites
    get_latest_release
    download_binary
    install_binary
    
    # Only update PATH for user installations
    if [ "$EUID" -ne 0 ]; then
        update_path
    fi
    
    test_installation
    cleanup
    print_success
}

# Handle Ctrl+C gracefully
trap 'echo -e "\n${RED}❌ Installation cancelled${NC}"; cleanup; exit 1' INT

# Parse command line arguments
FORCE_INSTALL=false
while [[ $# -gt 0 ]]; do
    case $1 in
        --force)
            FORCE_INSTALL=true
            shift
        ;;
        --help|-h)
            echo "qBoot Installation Script"
            echo ""
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --force    Force installation even if qBoot is already installed"
            echo "  --help     Show this help message"
            echo ""
            exit 0
        ;;
        *)
            echo -e "${RED}❌ Unknown option: $1${NC}"
            echo "Use --help for usage information"
            exit 1
        ;;
    esac
done

# Check if qBoot is already installed
if command -v qboot >/dev/null 2>&1 && [ "$FORCE_INSTALL" = false ]; then
    echo -e "${YELLOW}⚠️  qBoot is already installed${NC}"
    echo -e "${CYAN}Current version: $(qboot --version 2>/dev/null || echo "unknown")${NC}"
    echo -e "${YELLOW}Use --force to reinstall${NC}"
    exit 0
fi

# Run main installation
main

echo -e "${GREEN}Happy bootstrapping! 🚀${NC}"