
package com.genome.project_genome.service;

import java.io.ByteArrayInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.stereotype.Service;

@Service
public class MetricsAnalyzer {

    private final GitHubService githubService;

    public MetricsAnalyzer(GitHubService githubService) {
        this.githubService = githubService;
    }

  public Map<String, Object> analyze(
        String owner, String repo, String branch) {

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Integer> filesByLanguage = new LinkedHashMap<>();
        Map<String, Long> linesByLanguage = new LinkedHashMap<>();

        String[] languages = {
            "Java", "Python", "JavaScript", "C", "C++"
        };

        for (String language : languages) {
            filesByLanguage.put(language, 0);
            linesByLanguage.put(language, 0L);
        }

        int sourceFiles = 0;
        long totalLines = 0;
        long blankLines = 0;

        try {
            // Download the repository archive
           byte[] archive =
        githubService.getRepositoryArchive(owner, repo, branch);

            // Read ZIP archive locally
            try (ZipInputStream zip = new ZipInputStream(
                    new ByteArrayInputStream(archive))) {

                ZipEntry entry;

                while ((entry = zip.getNextEntry()) != null) {

                    if (entry.isDirectory()) {
                        continue;
                    }

                    String path = entry.getName();
                    String language = identifyLanguage(path);

                    if (language == null) {
                        continue;
                    }

                    sourceFiles++;

                    filesByLanguage.put(
                        language,
                        filesByLanguage.get(language) + 1
                    );

                    long fileLines = 0;
                    long fileBlankLines = 0;

                   BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(
                zip, StandardCharsets.UTF_8));

String line;

while ((line = reader.readLine()) != null) {
    fileLines++;

    if (line.isBlank()) {
        fileBlankLines++;
    }
}

                    totalLines += fileLines;
                    blankLines += fileBlankLines;

                    linesByLanguage.put(
                        language,
                        linesByLanguage.get(language) + fileLines
                    );

                    zip.closeEntry();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(
                "Unable to analyse repository archive: " + e.getMessage(),
                e
            );
        }

        result.put("sourceFiles", sourceFiles);
        result.put("totalLines", totalLines);
        result.put("blankLines", blankLines);
        result.put("linesOfCode", totalLines - blankLines);
        result.put("filesByLanguage", filesByLanguage);
        result.put("linesByLanguage", linesByLanguage);

        return result;
    }

    private String identifyLanguage(String path) {

        String file = path.toLowerCase();

        if (file.endsWith(".java")) return "Java";

        if (file.endsWith(".py")) return "Python";

        if (file.endsWith(".js")
                || file.endsWith(".jsx")
                || file.endsWith(".mjs")
                || file.endsWith(".cjs")) {
            return "JavaScript";
        }

        if (file.endsWith(".c")) return "C";

        if (file.endsWith(".cpp")
                || file.endsWith(".cc")
                || file.endsWith(".cxx")) {
            return "C++";
        }

        return null;
    }
}

