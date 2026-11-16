
package com.genome.project_genome.service;

import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Base64;

@Service
public class GitHubService {

    private final RestClient restClient = RestClient.create();

    public JsonNode getRepository(String owner, String repo) {

        String url = "https://api.github.com/repos/{owner}/{repo}";

        return restClient.get()
                .uri(url, owner, repo)
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .body(JsonNode.class);
    }

    public JsonNode getRepositoryTree(String owner, String repo) {

        // Get the repository's default branch
        JsonNode repository = getRepository(owner, repo);

        String branch = repository.get("default_branch").asText();

        // Get the complete directory structure
        String url = "https://api.github.com/repos/{owner}/{repo}/git/trees/{branch}?recursive=1";

        return restClient.get()
                .uri(url, owner, repo, branch)
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .body(JsonNode.class);
    }


public String getFileContent(String owner, String repo, String path) {

    JsonNode repository = getRepository(owner, repo);
    String branch = repository.get("default_branch").asText();

    String url = "https://api.github.com/repos/{owner}/{repo}/contents/{path}?ref={branch}";

    try {
        JsonNode file = restClient.get()
                .uri(url, owner, repo, path, branch)
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .body(JsonNode.class);

        if (file == null || !file.has("content")) {
            return null;
        }

        String encodedContent = file.get("content").asText();

        byte[] decodedBytes =
                Base64.getMimeDecoder().decode(encodedContent);

        return new String(
                decodedBytes,
                java.nio.charset.StandardCharsets.UTF_8
        );

    } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
        return null;
    }
}

public JsonNode getRecentCommits(String owner, String repo) {

    String url = "https://api.github.com/repos/{owner}/{repo}/commits?per_page=10";

    return restClient.get()
            .uri(url, owner, repo)
            .header("Accept", "application/vnd.github+json")
            .retrieve()
            .body(JsonNode.class);
}

public JsonNode getContributors(String owner, String repo) {

    String url = "https://api.github.com/repos/{owner}/{repo}/contributors?per_page=10";

    return restClient.get()
            .uri(url, owner, repo)
            .header("Accept", "application/vnd.github+json")
            .retrieve()
            .body(JsonNode.class);
}

public JsonNode getReleases(String owner, String repo) {

    String url = "https://api.github.com/repos/{owner}/{repo}/releases?per_page=5";

    return restClient.get()
            .uri(url, owner, repo)
            .header("Accept", "application/vnd.github+json")
            .retrieve()
            .body(JsonNode.class);
}

public byte[] getRepositoryArchive(
        String owner, String repo, String branch) {

    String url =
            "https://codeload.github.com/{owner}/{repo}/zip/refs/heads/{branch}";

    return restClient.get()
            .uri(url, owner, repo, branch)
            .retrieve()
            .body(byte[].class);
}

}



