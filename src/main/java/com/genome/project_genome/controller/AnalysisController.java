
package com.genome.project_genome.controller;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.databind.JsonNode;

import com.genome.project_genome.service.GitHubService;
import com.genome.project_genome.service.RepositoryAnalyzer;
import com.genome.project_genome.service.DependencyAnalyzer;
import com.genome.project_genome.service.ActivityAnalyzer;
import com.genome.project_genome.service.QualityAnalyzer;
import com.genome.project_genome.service.MetricsAnalyzer;
@RestController
public class AnalysisController {

    private final GitHubService githubService;
    private final RepositoryAnalyzer repositoryAnalyzer;
    private final DependencyAnalyzer dependencyAnalyzer;
    private final ActivityAnalyzer activityAnalyzer;
private final QualityAnalyzer qualityAnalyzer;
private final MetricsAnalyzer metricsAnalyzer;
   public AnalysisController(
        GitHubService githubService,
        RepositoryAnalyzer repositoryAnalyzer,
        DependencyAnalyzer dependencyAnalyzer,
        ActivityAnalyzer activityAnalyzer,
        QualityAnalyzer qualityAnalyzer,
        MetricsAnalyzer metricsAnalyzer) {

    this.githubService = githubService;
    this.repositoryAnalyzer = repositoryAnalyzer;
    this.dependencyAnalyzer = dependencyAnalyzer;
    this.activityAnalyzer = activityAnalyzer;
    this.qualityAnalyzer = qualityAnalyzer;
    this.metricsAnalyzer = metricsAnalyzer;
}
    @GetMapping("/api/health")
    public String health() {
        return "Project Genome backend is running!";
    }

    @GetMapping("/api/repository/{owner}/{repo}")
    public JsonNode getRepository(
            @PathVariable String owner,
            @PathVariable String repo) {

        return githubService.getRepository(owner, repo);
    }

    @GetMapping("/api/repository/{owner}/{repo}/tree")
    public JsonNode getRepositoryTree(
            @PathVariable String owner,
            @PathVariable String repo) {

        return githubService.getRepositoryTree(owner, repo);
    }

    
@GetMapping("/api/analyze/{owner}/{repo}")
public Map<String, Object> analyzeRepository(
        @PathVariable String owner,
        @PathVariable String repo) {

    Map<String, Object> report = new LinkedHashMap<>();

    // 1. Repository information
    JsonNode repository = githubService.getRepository(owner, repo);
    report.put("repository", repository);

    // 2. Repository structure and languages
    JsonNode tree = githubService.getRepositoryTree(owner, repo);
    Map<String, Object> structure = repositoryAnalyzer.analyze(tree);
    report.put("structure", structure);

    // 3. Dependency analysis
    Map<String, Object> dependencies =
            analyzeDependencies(owner, repo);
    report.put("dependencyAnalysis", dependencies);

// 4. Development activity
JsonNode commits =
        githubService.getRecentCommits(owner, repo);

JsonNode contributors =
        githubService.getContributors(owner, repo);

JsonNode releases =
        githubService.getReleases(owner, repo);

Map<String, Object> activity =
        activityAnalyzer.analyze(commits, contributors, releases);

report.put("developmentActivity", activity);
// 5. Quality indicators


Map<String, Object> quality =
        qualityAnalyzer.analyze(tree);

report.put("qualityIndicators", quality);
// 6. Code metrics
String branch = repository.get("default_branch").asText();

Map<String, Object> metrics =
        metricsAnalyzer.analyze(owner, repo, branch);

report.put("codeMetrics", metrics);
return report;
}


    
@GetMapping("/api/analyze/{owner}/{repo}/dependencies")
public Map<String, Object> analyzeDependencies(
        @PathVariable String owner,
        @PathVariable String repo) {

    Map<String, Object> result = new LinkedHashMap<>();

    String pom = githubService.getFileContent(
            owner, repo, "pom.xml");

    if (pom != null) {
        result.put("manifest", "pom.xml");
        result.put("ecosystem", "Maven");
        result.put("dependencies",
                dependencyAnalyzer.analyzeMavenPom(pom));
        return result;
    }

    String gradle = githubService.getFileContent(
            owner, repo, "build.gradle");

    if (gradle == null) {
        gradle = githubService.getFileContent(
                owner, repo, "build.gradle.kts");
    }

    if (gradle != null) {
    result.put("manifest", "Gradle build file");
    result.put("ecosystem", "Gradle");
    result.put("dependencies",
            dependencyAnalyzer.analyzeGradleBuild(gradle));
    return result;
}

    String packageJson = githubService.getFileContent(
            owner, repo, "package.json");

   if (packageJson != null) {
    result.put("manifest", "package.json");
    result.put("ecosystem", "npm");
    result.put("dependencies",
            dependencyAnalyzer.analyzeNpmPackage(packageJson));
    return result;
}
    String requirements = githubService.getFileContent(
            owner, repo, "requirements.txt");

    if (requirements != null) {
    result.put("manifest", "requirements.txt");
    result.put("ecosystem", "pip");
    result.put("dependencies",
            dependencyAnalyzer.analyzePythonRequirements(requirements));
    return result;
}
String conan = githubService.getFileContent(
        owner, repo, "conanfile.txt");

if (conan != null) {
    result.put("manifest", "conanfile.txt");
    result.put("ecosystem", "Conan");
    result.put("dependencies",
            dependencyAnalyzer.analyzeConanRequirements(conan));
    return result;
}
String vcpkg = githubService.getFileContent(
        owner, repo, "vcpkg.json");

if (vcpkg != null) {
    result.put("manifest", "vcpkg.json");
    result.put("ecosystem", "vcpkg");
    result.put("dependencies",
            dependencyAnalyzer.analyzeVcpkgJson(vcpkg));
    return result;
}
    result.put("manifest", "Not found");
    result.put("message",
            "No supported dependency manifest found.");

    return result;
}

}

