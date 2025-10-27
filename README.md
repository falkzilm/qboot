# qBoot 🚀

[![Build Status](https://github.com/falkzilm/qboot/workflows/CI/badge.svg)](https://github.com/falkzilm/qboot/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.org)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.28.4-blue.svg)](https://quarkus.io)

> **A powerful CLI tool for bootstrapping projects from templates - built by a developer, for developers**

qBoot is a modern, fast, and flexible command-line interface that I created to help developers quickly create new projects from predefined templates. Whether you're starting a new Quarkus microservice, React app, Spring Boot API, Vue.js frontend, Node.js backend, ASP.NET Core service, Kotlin/Ktor project, or pure Java application, qBoot gets you up and running in seconds.

## 🌟 Features

- **🚀 Rapid Project Generation** - Bootstrap complete projects in seconds
- **🌍 Remote Templates** - Support for HTTP/HTTPS template URLs  
- **📁 Local Templates** - Use local XML template files
- **⚡ Cross-Platform** - Native support for Windows, macOS, and Linux
- **🔧 Customizable** - Override project names, packages, and configurations
- **🏗️ Multi-Workspace** - Generate multiple related projects at once
- **🛠️ Multi-Framework** - Support for 9 popular frameworks: Quarkus, Angular, React, Vue.js, Spring Boot, Node.js, ASP.NET Core, Kotlin
- **📦 Native Compilation** - Compile to native executables for fast startup

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Maven 3.9+
- (Optional) GraalVM for native compilation

### Installation

#### Quick Install (Recommended)

**Unix/Linux/macOS:**
```bash
curl -sSL https://raw.githubusercontent.com/falkzilm/qboot/main/install.sh | bash
```

**Windows (PowerShell as Administrator):**
```powershell
Set-ExecutionPolicy Bypass -Scope Process -Force; 
iex ((New-Object System.Net.WebClient).DownloadString('https://raw.githubusercontent.com/falkzilm/qboot/main/install.ps1'))
```

#### Manual Installation

**Download Pre-built Binary:**
```bash
# Linux
curl -L -o qboot https://github.com/falkzilm/qboot/releases/latest/download/qboot-linux
chmod +x qboot && sudo mv qboot /usr/local/bin/

# macOS
curl -L -o qboot https://github.com/falkzilm/qboot/releases/latest/download/qboot-macos
chmod +x qboot && sudo mv qboot /usr/local/bin/

# Windows (download and add to PATH)
# Download: https://github.com/falkzilm/qboot/releases/latest/download/qboot-windows.exe
```

#### Build from Source

```bash
git clone https://github.com/falkzilm/qboot.git
cd qboot
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

### Basic Usage

```bash
# List available frameworks
qboot --list

# Create a React project from a template
qboot create -t ./react-template.xml -n my-react-app

# Create a Spring Boot API from a template
qboot create -t ./springboot-template.xml -n my-api -p com.example

# Create a project from a remote template
qboot create -t https://raw.githubusercontent.com/example/templates/main/quarkus-rest.xml \
  -n my-microservice -p com.example.microservice

# Show help
qboot --help
```

## 📖 Usage Guide

### Command Line Options

```
Usage: qboot [OPTIONS] COMMAND

Options:
  -h, --help     Show this help message and exit
  -v, --version  Print version information and exit
  -l, --list     List all available framework engines

Commands:
  create  Bootstrap a new project from a template

Create Command Options:
  -t, --template <template>     Template file path or HTTP/HTTPS URL (required)
  -n, --name <name>            Project name (overrides template default)
  -p, --package <package>      Package name (overrides template default)
  -c, --cli-args <args>        Additional CLI arguments for framework tools
  -o, --output <directory>     Output directory (default: current directory)
  -d, --debug                  Enable verbose debug output
```

### Template Format

qBoot uses XML templates to define project structure and configuration. The repository includes complete example templates:

- **[quarkus-tpl.xml](./quarkus-tpl.xml)** - Comprehensive Quarkus REST API template with dependencies, custom structure modifications, and Maven configuration
- **[angular-tpl.xml](./angular-tpl.xml)** - Angular SPA template with npm dependencies and project scaffolding  
- **[quarkus-angular-spa.xml](./quarkus-angular-spa.xml)** - Full-stack multi-workspace template combining Quarkus backend and Angular frontend
- **[react-nextjs-tpl.xml](./react-nextjs-tpl.xml)** - React/Next.js template with TypeScript, Tailwind CSS, and development environment setup
- **[kotlin-ktor-tpl.xml](./kotlin-ktor-tpl.xml)** - Kotlin/Ktor web API template with Gradle build system and JSON serialization

These templates demonstrate all template features including dependency management, structure customization, and multi-workspace projects. Use them as starting points for creating your own templates.

### Supported Frameworks

qBoot supports 9 popular frameworks with specialized engines:

| Framework | Aliases | Description | CLI Example |
|-----------|---------|-------------|-------------|
| **Quarkus** | `quarkus` | Supersonic Java microservices | `<framework>quarkus</framework>` |
| **Angular** | `angular`, `ng` | TypeScript frontend framework | `<framework>angular</framework>` |
| **React** | `react`, `reactjs`, `nextjs`, `next` | JavaScript library for UIs | `<framework>react</framework>` |
| **Vue.js** | `vue`, `vuejs` | Progressive JavaScript framework | `<framework>vue</framework>` |
| **Spring Boot** | `springboot`, `spring`, `spring-boot` | Java enterprise framework | `<framework>springboot</framework>` |
| **Node.js** | `nodejs`, `node`, `express` | JavaScript server runtime | `<framework>nodejs</framework>` |
| **ASP.NET Core** | `dotnet`, `aspnet`, `aspnetcore`, `net` | .NET web framework | `<framework>dotnet</framework>` |
| **Kotlin** | `kotlin`, `ktor` | Modern JVM language with Ktor | `<framework>kotlin</framework>` |

#### Framework-Specific CLI Arguments

Each framework supports specific CLI arguments for customization:

**React:**
- `--nextjs` - Use Next.js instead of Create React App
- `--vite` - Use Vite + React template
- `--typescript` - Enable TypeScript support

**Vue.js:**
- `--vue3` or `--vite` - Use Vue 3 + Vite template
- `--typescript` - Enable TypeScript support
- `--router` - Include Vue Router

**Spring Boot:**
- `--deps=web,data-jpa,security` - Specify dependencies
- `--java=21` - Java version (default: 21)
- `--boot-version=3.4.1` - Spring Boot version
- `--packaging=jar` - Packaging type (jar/war)

**Node.js:**
- `--typescript` or `--ts` - Use TypeScript
- `--mongodb` - Include Mongoose
- `--postgres` - Include PostgreSQL support
- `--cors`, `--morgan` - Include middleware

**ASP.NET Core:**
- `--template=webapi` - Project template (webapi, mvc, blazor, etc.)
- `--framework=net8.0` - .NET version
- `--auth` - Include authentication
- `--ef` - Include Entity Framework

**Kotlin:**
- `--ktor` - Use Ktor framework (default: basic Kotlin)
- `--spring` - Use Spring Boot with Kotlin
- `--kotlin-version=2.1.0` - Kotlin version
- `--ktor-version=3.0.3` - Ktor version

#### Basic Template Structure

```xml
<qtemplate>
    <workspaces>
        <workspace>
            <general>
                <framework>quarkus</framework>
                <projectName>my-service</projectName>
                <projectPackage>com.example</projectPackage>
                <frameworkVersion>3.28.4</frameworkVersion>
                <cliArgs>--extensions=rest,jackson</cliArgs>
            </general>
            
            <!-- Pre-requisite dependencies (checked before generation) -->
            <dependencies name="pre">
                <java>21+</java>
                <mvn>3.9+</mvn>
            </dependencies>
            
            <!-- Post-generation dependencies (installed after project creation) -->
            <dependencies name="post">
                <lombok packageName="org.projectlombok:lombok" optional="true">1.18.30</lombok>
            </dependencies>
            
            <!-- Custom file modifications -->
            <structure value="custom">
                <changeset type="add">
                    <path name="src/main/resources/application.yml"><![CDATA[
server:
  port: 8080
logging:
  level:
    com.example: DEBUG
                    ]]></path>
                </changeset>
                <changeset type="remove">
                    <path name="src/main/resources/application.properties" />
                </changeset>
            </structure>
        </workspace>
    </workspaces>
</qtemplate>
```

### Multi-Workspace Projects

Create full-stack applications with backend and frontend in one command:

```xml
<qtemplate>
    <workspaces>
        <workspace>
            <path>backend</path>
            <general>
                <framework>quarkus</framework>
                <projectName>my-api</projectName>
                <projectPackage>com.example.api</projectPackage>
            </general>
            <!-- ... backend configuration ... -->
        </workspace>
        <workspace>
            <path>frontend</path>
            <general>
                <framework>angular</framework>
                <projectName>my-app</projectName>
                <frameworkVersion>17.0.0</frameworkVersion>
            </general>
            <!-- ... frontend configuration ... -->
        </workspace>
    </workspaces>
</qtemplate>
```

## 🔧 Development & Extension

### Building the Project

```bash
# Development build
./mvnw compile quarkus:dev

# Production JAR
./mvnw package

# Native executable
./mvnw package -Dnative
```

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=TemplateServiceTest

# Run with coverage
./mvnw test jacoco:report
```

### Extending qBoot with New Frameworks

qBoot is designed to be easily extensible for new frameworks. Here's how to add support for a new framework:

#### 1. Create a Generation Engine

Create a new class extending `GenerationEngine`:

```java
package de.falkzilm.gen.myframework;

import de.falkzilm.gen.GenerationEngine;
import de.falkzilm.gen.GenerationParameters;
import de.falkzilm.helper.OsUtils;
import org.apache.commons.exec.CommandLine;

public class MyFrameworkEngine extends GenerationEngine {
    
    @Override
    public void generate(GenerationParameters genParameters) {
        // Create project directory
        CommandLine mkdirCmd = OsUtils.createMkdirCommand(
            genParameters.target().resolve(genParameters.name()).toString()
        );
        executeCommand(mkdirCmd, genParameters);
        
        // Execute framework-specific commands
        String frameworkArgs = String.format("create %s %s", 
            genParameters.name(), 
            genParameters.cliArgs()
        );
        CommandLine frameworkCmd = OsUtils.createShellCommand("myframework " + frameworkArgs);
        executeCommand(frameworkCmd, genParameters);
    }
    
    @Override
    public String getFrameworkName() {
        return "myframework";
    }
    
    @Override
    public boolean isAvailable() {
        // Check if framework CLI is available
        return checkCommandAvailability("myframework");
    }
}
```

#### 2. Register the Engine

Annotate your Engine with `@FrameworkUsage()`:

```java
import de.falkzilm.gen.Framework;
import de.falkzilm.gen.FrameworkUsage;

@FrameworkUsage(Framework.MYFRAMEWORK)
public class NewEngine {

}
```

#### 3. Add Framework to Enum

Update the `Framework` enum:

```java
public enum Framework {
    QUARKUS("quarkus"),
    ANGULAR("angular"),
    MYFRAMEWORK("myframework");  // Add your framework
    
    private final String name;
    
    Framework(String name) {
        this.name = name;
    }
}
```

#### 4. Create Dependency Handler (Optional)

If your framework needs custom dependency management, create a handler:

```java
package de.falkzilm.gen.myframework;

import de.falkzilm.gen.DependencyHandler;
import de.falkzilm.template.Dependency;

@SuperBuilder
public class MyFrameworkDependencyHandler extends DependencyHandler {
    
    @Override
    protected void frameworkInstall(Dependency dep) {
        // Implement framework-specific dependency installation
        String packageManager = "my-package-manager";
        String installCommand = packageManager + " install " + dep.packageName() + "@" + dep.version();
        
        CommandLine cmd = OsUtils.createShellCommand(installCommand);
        RunWrapper.builder()
                .cmd(cmd)
                .build()
                .run(getGenParameters().target(), getGenParameters().debug());
    }
}
```

#### 5. Cross-Platform Considerations

Use `OsUtils` for cross-platform compatibility:

```java
// OS-aware command creation
CommandLine cmd = OsUtils.isWindows() 
    ? OsUtils.createShellCommand("myframework.exe " + args)
    : OsUtils.createShellCommand("myframework " + args);

// Use proper executable names
String executable = OsUtils.normalizeCommandName("myframework");
```

#### Key Design Patterns

- **OS Abstraction**: Always use `OsUtils` for command execution
- **Command Validation**: Implement `isAvailable()` to check prerequisites
- **Error Handling**: Use `RunWrapper` for consistent command execution
- **Dependency Management**: Extend `DependencyHandler` for package management
- **Template Integration**: Support all XML template features (dependencies, structure modifications)

### Project Structure

```
qboot/
├── src/main/java/de/falkzilm/
│   ├── BootstrapCmd.java           # Main CLI command
│   ├── cmds/CreateCmd.java         # Create subcommand
│   ├── gen/                        # Generation engines
│   │   ├── EngineFactory.java      # Engine registry
│   │   ├── Framework.java          # Supported frameworks
│   │   └── java/QuarkusEngine.java # Quarkus implementation
│   ├── template/                   # Template handling
│   │   ├── TemplateService.java    # Template loading/validation
│   │   └── QTemplate.java          # Template model
│   └── helper/ConsoleFormatter.java # Output formatting
├── src/test/java/                  # Comprehensive test suite
└── examples/                       # Example templates
```

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Setup

```bash
git clone https://github.com/falkzilm/qboot.git
cd qboot
./mvnw compile quarkus:dev
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2024 qBoot Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 🙏 Acknowledgments

- Built with [Quarkus](https://quarkus.io/) - The Supersonic Subatomic Java Framework
- CLI powered by [PicoCLI](https://picocli.info/)
- Native compilation with [GraalVM](https://www.graalvm.org/)

## 📊 Status

- ✅ Core template processing
- ✅ Local file templates  
- ✅ HTTP/HTTPS remote templates
- ✅ Cross-platform native compilation
- ✅ **9 Framework Engines:**
  - ✅ Quarkus (Java microservices)
  - ✅ Angular (TypeScript frontend)
  - ✅ React/Next.js (JavaScript UI library)
  - ✅ Vue.js (Progressive framework)
  - ✅ Spring Boot (Java enterprise)
  - ✅ Node.js/Express (JavaScript backend)
  - ✅ ASP.NET Core (.NET web framework)
  - ✅ Kotlin/Ktor (Modern JVM language)
- ✅ Multi-workspace support
- ✅ Comprehensive test suite
- 🚧 Template repository/marketplace
- 📋 Interactive template wizard

---

<p align="center">
  Made with ❤️<br>
  <a href="https://github.com/falkzilm/qboot">⭐ Star us on GitHub</a>
</p>
