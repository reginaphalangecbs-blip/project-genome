
package com.genome.project_genome.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;

@Service
public class RepositoryAnalyzer {

    public Map<String, Object> analyze(JsonNode repositoryTree) {

        int totalFiles = 0;
        int totalFolders = 0;
        long totalSize = 0;

        Map<String, Integer> languageCounts = new LinkedHashMap<>();

        languageCounts.put("Java", 0);
        languageCounts.put("Python", 0);
        languageCounts.put("JavaScript", 0);
        languageCounts.put("C", 0);
        languageCounts.put("C++", 0);
        languageCounts.put("Other", 0);

        JsonNode tree = repositoryTree.get("tree");

        for (JsonNode item : tree) {

            String path = item.get("path").asText();
            String type = item.get("type").asText();

            if (type.equals("tree")) {
                totalFolders++;
            }

            else if (type.equals("blob")) {

                totalFiles++;

                if (item.has("size")) {
                    totalSize += item.get("size").asLong();
                }

                String language = identifyLanguage(path);

                languageCounts.put(
                    language,
                    languageCounts.get(language) + 1
                );
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("totalFiles", totalFiles);
        result.put("totalFolders", totalFolders);
        result.put("totalSizeBytes", totalSize);
        result.put("languageDistribution", languageCounts);

        return result;
    }

    private String identifyLanguage(String path) {

        String file = path.toLowerCase();

        if (file.endsWith(".java")) return "Java";

        if (file.endsWith(".py")) return "Python";

        if (file.endsWith(".js") ||
            file.endsWith(".jsx") ||
            file.endsWith(".mjs") ||
            file.endsWith(".cjs")) {
            return "JavaScript";
        }

        if (file.endsWith(".c")) return "C";

        if (file.endsWith(".cpp") ||
            file.endsWith(".cc") ||
            file.endsWith(".cxx") ||
            file.endsWith(".hpp") ||
            file.endsWith(".hh") ||
            file.endsWith(".hxx")) {
            return "C++";
        }

        return "Other";
    }
}

