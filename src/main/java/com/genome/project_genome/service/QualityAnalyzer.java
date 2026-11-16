
package com.genome.project_genome.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;

@Service
public class QualityAnalyzer {

    public Map<String, Object> analyze(JsonNode repositoryTree) {

        Map<String, Object> result = new LinkedHashMap<>();
        List<String> observations = new ArrayList<>();

        boolean hasReadme = false;
        boolean hasLicense = false;
        boolean hasTests = false;
        boolean hasDocumentation = false;
        boolean hasCI = false;

        int testFilesFound = 0;

        JsonNode tree = repositoryTree.get("tree");

        if (tree != null && tree.isArray()) {

            for (JsonNode item : tree) {

                String path = item.get("path").asText();
                String lowerPath = path.toLowerCase();
                String fileName = lowerPath.substring(
                        lowerPath.lastIndexOf('/') + 1
                );

                // README detection
                if (fileName.startsWith("readme")) {
                    hasReadme = true;
                }

                // License detection
                if (fileName.equals("license")
                        || fileName.startsWith("license.")
                        || fileName.startsWith("licence")) {
                    hasLicense = true;
                }

                // Test file detection
                boolean testFile =
                        lowerPath.contains("/test/")
                        || lowerPath.startsWith("test/")
                        || fileName.endsWith("test.java")
                        || fileName.endsWith("_test.py")
                        || fileName.endsWith(".test.js")
                        || fileName.endsWith(".spec.js")
                        || fileName.endsWith("_test.c")
                        || fileName.endsWith("_test.cpp");

                if (testFile) {
                    hasTests = true;

                    if ("blob".equals(item.get("type").asText())) {
                        testFilesFound++;
                    }
                }

                // Documentation detection
                if (lowerPath.startsWith("docs/")
                        || lowerPath.contains("/docs/")
                        || fileName.startsWith("documentation")) {
                    hasDocumentation = true;
                }

                // Continuous integration detection
                if (lowerPath.startsWith(".github/workflows/")
                        || fileName.equals(".gitlab-ci.yml")
                        || fileName.equals("jenkinsfile")
                        || fileName.equals(".travis.yml")) {
                    hasCI = true;
                }
            }
        }

        if (hasReadme) {
            observations.add("A README file is present.");
        } else {
            observations.add("No README file was detected.");
        }

        if (hasLicense) {
            observations.add("A license file is present.");
        } else {
            observations.add("No license file was detected.");
        }

        if (hasTests) {
            observations.add("Test-related files or directories were detected.");
        } else {
            observations.add("No test-related files were detected.");
        }

        if (hasDocumentation) {
            observations.add("Documentation-related files or directories were detected.");
        } else {
            observations.add("No separate documentation directory or file was detected.");
        }

        if (hasCI) {
            observations.add("A recognized CI configuration was detected.");
        } else {
            observations.add("No recognized CI configuration was detected.");
        }

        result.put("hasReadme", hasReadme);
        result.put("hasLicense", hasLicense);
        result.put("hasTests", hasTests);
        result.put("testFilesFound", testFilesFound);
        result.put("hasDocumentation", hasDocumentation);
        result.put("hasCI", hasCI);
        result.put("observations", observations);

        return result;
    }
}

