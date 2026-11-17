# Project Genome

Project Genome is a repository analysis tool that examines public GitHub repositories and presents their technical structure, dependencies, development activity, and basic code metrics in a unified dashboard.

## Features

- **Repository Overview:** Displays repository information and metadata.
- **Structure Analysis:** Identifies files, folders, and programming languages.
- **Dependency Analysis:** Detects dependencies from supported package and build files.
- **Development Activity:** Summarizes recent commits, contributors, and releases.
- **Quality Indicators:** Checks for README, license, tests, documentation, and CI configuration.
- **Code Metrics:** Calculates basic source-code statistics.

## Technology Stack

- Java
- Spring Boot
- Maven
- GitHub REST API
- HTML
- CSS
- JavaScript

## Supported Dependency Files

- `pom.xml`
- `build.gradle`
- `package.json`
- `requirements.txt`
- `conanfile.txt`
- `vcpkg.json`

## Getting Started

### Prerequisites

- Java JDK 25 or compatible version
- Git
- Internet connection

### Run Locally

1. Clone the repository:

   ```bash
   git clone https://github.com/reginaphalangecbs-blip/project-genome.git
   ```

2. Open the project directory:

   ```bash
   cd project-genome
   ```

3. Start the Spring Boot application on Windows:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   On macOS/Linux:

   ```bash
   ./mvnw spring-boot:run
   ```

4. Open the dashboard:

   http://localhost:8080/

5. Enter a public GitHub repository URL and click **Analyse Repository**.

## API Endpoint

```text
GET /api/analyze/{owner}/{repo}
```

Example:

```text
http://localhost:8080/api/analyze/octocat/Hello-World
```

## Project Status

Project Genome is currently an MVP demonstrating automated analysis of public GitHub repositories. Its analysis is based on available repository metadata, supported dependency manifests, and basic rule-based indicators.

## Future Scope

- Repository comparison
- More programming languages and package formats
- Advanced code quality analysis
- Security and vulnerability analysis
- Historical repository insights

---

Developed as a student software project.