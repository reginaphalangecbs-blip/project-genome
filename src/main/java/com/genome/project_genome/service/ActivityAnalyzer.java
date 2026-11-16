
package com.genome.project_genome.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
public class ActivityAnalyzer {

    public Map<String, Object> analyze(
            JsonNode commits,
            JsonNode contributors,
            JsonNode releases) {

        Map<String, Object> result = new LinkedHashMap<>();

        // Recent commits
        List<Map<String, String>> commitList = new ArrayList<>();

        String latestCommitDate = "Not available";

        if (commits != null && commits.isArray()) {

            for (JsonNode commit : commits) {

                Map<String, String> item = new LinkedHashMap<>();

                item.put("sha", getText(commit, "sha"));

                JsonNode details = commit.get("commit");

                item.put("message", getText(details, "message"));

                JsonNode author = details != null
                        ? details.get("author") : null;

                item.put("author", getText(author, "name"));
                item.put("date", getText(author, "date"));

                if (latestCommitDate.equals("Not available")) {
                    latestCommitDate = getText(author, "date");
                }

                commitList.add(item);
            }
        }

        // Contributors
        List<Map<String, Object>> contributorList =
                new ArrayList<>();

        if (contributors != null && contributors.isArray()) {

            for (JsonNode contributor : contributors) {

                Map<String, Object> item = new LinkedHashMap<>();

                item.put("username",
                        getText(contributor, "login"));

                item.put("contributions",
                        contributor.has("contributions")
                        ? contributor.get("contributions").asInt()
                        : 0);

                contributorList.add(item);
            }
        }

        // Releases
        List<Map<String, String>> releaseList =
                new ArrayList<>();

        if (releases != null && releases.isArray()) {

            for (JsonNode release : releases) {

                Map<String, String> item = new LinkedHashMap<>();

                item.put("name", getText(release, "name"));
                item.put("tag", getText(release, "tag_name"));
                item.put("publishedAt",
                        getText(release, "published_at"));

                releaseList.add(item);
            }
        }

        result.put("recentCommits", commitList);
        result.put("recentCommitCount", commitList.size());
        result.put("latestCommitDate", latestCommitDate);

        result.put("contributorsReturned", contributorList.size());
        result.put("contributors", contributorList);

        result.put("releasesReturned", releaseList.size());
        result.put("releases", releaseList);

        return result;
    }

    private String getText(JsonNode node, String field) {

        if (node == null || !node.has(field)
                || node.get(field).isNull()) {
            return "Not available";
        }

        return node.get(field).asText();
    }
}

