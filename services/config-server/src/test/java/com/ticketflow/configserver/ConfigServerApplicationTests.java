package com.ticketflow.configserver;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The LOCAL FILES backend (profile "native"): serves the config-repo/ folder copied into the jar.
 * The default backend is Git, see {@link ConfigServerGitBackendTest}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.profiles.active=native",
        "spring.cloud.config.server.native.search-locations=classpath:/config-repo/",
        // The git block of application.yml is not used here; a value avoids resolving CONFIG_GIT_URI
        "spring.cloud.config.server.git.uri=not-used-with-the-native-profile"
})
class ConfigServerApplicationTests {

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
    void healthEndpointIsUp() throws Exception {
        HttpResponse<String> response = get("/actuator/health");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("UP");
    }

    @Test
    void servesTheSharedConfigurationToEveryApplication() throws Exception {
        HttpResponse<String> response = get("/any-service/default");

        assertThat(response.statusCode()).isEqualTo(200);
        // application.yml is shared by all services
        assertThat(response.body()).contains("management.endpoints.web.exposure.include");
    }

    @Test
    void servesTheConfigurationSpecificToAService() throws Exception {
        HttpResponse<String> response = get("/customer-service/default");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("ticketflow.demo.message");
    }
}
