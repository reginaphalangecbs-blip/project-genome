
package com.genome.project_genome.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class DependencyAnalyzer {

    public List<Map<String, String>> analyzeMavenPom(String pomContent) {

        List<Map<String, String>> dependencies = new ArrayList<>();

        Pattern dependencyPattern = Pattern.compile(
            "<dependency>(.*?)</dependency>",
            Pattern.DOTALL
        );

        Matcher matcher = dependencyPattern.matcher(pomContent);

        while (matcher.find()) {

            String dependencyBlock = matcher.group(1);

            String groupId = extractValue(dependencyBlock, "groupId");
            String artifactId = extractValue(dependencyBlock, "artifactId");
            String version = extractValue(dependencyBlock, "version");
            String scope = extractValue(dependencyBlock, "scope");

            if (groupId != null && artifactId != null) {

                Map<String, String> dependency = new LinkedHashMap<>();

                dependency.put("groupId", groupId);
                dependency.put("artifactId", artifactId);
                dependency.put("version",
                    version != null ? version : "Managed / Not specified");
                dependency.put("scope",
                    scope != null ? scope : "compile");

                dependencies.add(dependency);
            }
        }

        return dependencies;
    }

    private String extractValue(String content, String tag) {

        Pattern pattern = Pattern.compile(
            "<" + tag + ">\\s*(.*?)\\s*</" + tag + ">",
            Pattern.DOTALL
        );

        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }
    public List<Map<String, String>> analyzeGradleBuild(String gradleContent) {

    List<Map<String, String>> dependencies = new ArrayList<>();

    Pattern pattern = Pattern.compile(
        "(implementation|api|testImplementation|runtimeOnly|compileOnly|annotationProcessor)\\s*[('\\\"]+([^'\\\"]+)['\\\"]+"
    );

    Matcher matcher = pattern.matcher(gradleContent);

    while (matcher.find()) {

        String scope = matcher.group(1);
        String coordinates = matcher.group(2).trim();

        String[] parts = coordinates.split(":");

        if (parts.length >= 2
        && !parts[0].isBlank()
        && !parts[1].isBlank()) {

            Map<String, String> dependency = new LinkedHashMap<>();

            dependency.put("groupId", parts[0]);
            dependency.put("artifactId", parts[1]);
            dependency.put("version",
                    parts.length >= 3 ? parts[2] : "Not specified");
            dependency.put("scope", scope);

            dependencies.add(dependency);
        }
    }

    return dependencies;
}
public List<Map<String, String>> analyzeNpmPackage(String packageContent) {

    List<Map<String, String>> dependencies = new ArrayList<>();

    try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(packageContent);

        String[] sections = {
            "dependencies",
            "devDependencies",
            "peerDependencies",
            "optionalDependencies"
        };

        for (String section : sections) {

            JsonNode sectionNode = root.get(section);

            if (sectionNode == null || !sectionNode.isObject()) {
                continue;
            }

            for (Map.Entry<String, JsonNode> entry :
        sectionNode.properties()) {

    String packageName = entry.getKey();
    String version = entry.getValue().asText();

                Map<String, String> dependency = new LinkedHashMap<>();

                dependency.put("groupId", "npm");
                dependency.put("artifactId", packageName);
                dependency.put("packageName", packageName);
                dependency.put("version", version);
                dependency.put("scope", section);

                dependencies.add(dependency);
            }
        }

    } catch (Exception e) {
        throw new IllegalArgumentException(
                "Unable to parse package.json", e);
    }

    return dependencies;
}

public List<Map<String, String>> analyzePythonRequirements(String content) {

    List<Map<String, String>> dependencies = new ArrayList<>();

    Pattern pattern = Pattern.compile(
        "^([A-Za-z0-9][A-Za-z0-9._-]*)(?:\\[[^]]+\\])?\\s*(.*)$"
    );

    for (String line : content.split("\\R")) {

        line = line.trim();

        if (line.isEmpty() || line.startsWith("#")
                || line.startsWith("-")) {
            continue;
        }

        Matcher matcher = pattern.matcher(line);

        if (matcher.matches()) {

            Map<String, String> dependency = new LinkedHashMap<>();

            dependency.put("groupId", "PyPI");
            dependency.put("artifactId", matcher.group(1));
            dependency.put("version",
                    matcher.group(2).isBlank()
                    ? "Not specified" : matcher.group(2).trim());
            dependency.put("scope", "runtime");

            dependencies.add(dependency);
        }
    }

    return dependencies;
}

public List<Map<String, String>> analyzeConanRequirements(String content) {

    List<Map<String, String>> dependencies = new ArrayList<>();

    boolean insideRequires = false;

    for (String line : content.split("\\R")) {

        line = line.trim();

        if (line.isEmpty() || line.startsWith("#")) {
            continue;
        }

        if (line.startsWith("[")) {
            insideRequires = line.equalsIgnoreCase("[requires]");
            continue;
        }

        if (insideRequires) {

            String[] parts = line.split("/", 2);

            Map<String, String> dependency = new LinkedHashMap<>();

            dependency.put("groupId", "Conan");
            dependency.put("artifactId", parts[0].trim());
            dependency.put("version",
                    parts.length > 1 ? parts[1].trim() : "Not specified");
            dependency.put("scope", "requires");

            dependencies.add(dependency);
        }
    }

    return dependencies;
}

public List<Map<String, String>> analyzeVcpkgJson(String content) {

    List<Map<String, String>> dependencies = new ArrayList<>();

    try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(content);
        JsonNode dependencyNodes = root.get("dependencies");

        if (dependencyNodes == null || !dependencyNodes.isArray()) {
            return dependencies;
        }

        for (JsonNode node : dependencyNodes) {

            String name;
            String version = "Not specified";

            if (node.isTextual()) {
                name = node.asText();
            } else if (node.isObject() && node.has("name")) {
                name = node.get("name").asText();

                if (node.has("version>=")) {
                    version = node.get("version>=").asText();
                }
            } else {
                continue;
            }

            Map<String, String> dependency = new LinkedHashMap<>();
            dependency.put("groupId", "vcpkg");
            dependency.put("artifactId", name);
            dependency.put("version", version);
            dependency.put("scope", "runtime");

            dependencies.add(dependency);
        }

    } catch (Exception e) {
        throw new IllegalArgumentException(
                "Unable to parse vcpkg.json", e);
    }

    return dependencies;
}

}
