package com.ticketflow.configserver;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * The GIT backend, tested against a real Git repository created in a temporary folder: no network,
 * no account, and it exercises what matters (clone, serve, pick up a new commit).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.cloud.config.server.git.default-label=main",
        // Fetch on every request, so the test does not wait for the refresh delay
        "spring.cloud.config.server.git.refresh-rate=0"
})
class ConfigServerGitBackendTest {

    private static final Path REPOSITORY = createRepository();

    @DynamicPropertySource
    static void gitRepository(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.config.server.git.uri", () -> REPOSITORY.toUri().toString());
    }

    @Value("${local.server.port}")
    int port;

    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Accept", "application/json")
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void servesTheConfigurationOfAnApplicationFromGit() throws Exception {
        HttpResponse<String> response = get("/demo-service/default");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("hello-from-git");
        // application.yml is shared by every application
        assertThat(response.body()).contains("shared-from-git");
    }

    @Test
    void picksUpANewCommitWithoutRestartingTheServer() throws Exception {
        assertThat(get("/late-service/default").body()).doesNotContain("pushed-later");

        commit("late-service.yml", "late:\n  value: pushed-later\n");

        assertThat(get("/late-service/default").body()).contains("pushed-later");
    }

    // ---- a throw-away Git repository ----

    private static Path createRepository() {
        try {
            // toRealPath(): on macOS the temp folder (/var/...) is a symbolic link to /private/var/...
            // and Spring Cloud Config refuses a repository whose path is not the resolved one.
            Path dir = Files.createTempDirectory("ticketflow-config-git-").toRealPath();
            try (Git git = Git.init().setDirectory(dir.toFile()).setInitialBranch("main").call()) {
                Files.writeString(dir.resolve("application.yml"), "shared:\n  value: shared-from-git\n");
                Files.writeString(dir.resolve("demo-service.yml"), "demo:\n  message: hello-from-git\n");
                commit(git, "initial configuration");
            }
            return dir;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (GitAPIException e) {
            throw new IllegalStateException("Cannot prepare the test repository", e);
        }
    }

    private static void commit(String file, String content) throws Exception {
        Files.writeString(REPOSITORY.resolve(file), content);
        try (Git git = Git.open(REPOSITORY.toFile())) {
            commit(git, "add " + file);
        }
    }

    private static void commit(Git git, String message) throws GitAPIException {
        git.add().addFilepattern(".").call();
        git.commit()
                .setMessage(message)
                .setAuthor("test", "test@example.com")
                .setCommitter("test", "test@example.com")
                // Whatever the developer's own Git config says (a signing key, for instance)
                .setSign(false)
                .call();
    }
}
