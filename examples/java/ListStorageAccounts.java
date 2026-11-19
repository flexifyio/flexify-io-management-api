// Requires Java 11+ and jackson-databind

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ListStorageAccounts {

    static final String API_URL = "https://api.flexify.io";
    static final String API_TOKEN = "<your API token>"; // replace with your API token

    public static void main(String[] args) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/storage-accounts?include-buckets=false"))
                .header("Authorization", "Bearer " + API_TOKEN)
                .timeout(Duration.ofSeconds(60))
                .build();
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(60)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        }

        for (JsonNode account : new ObjectMapper().readTree(response.body())) {
            System.out.println(account.get("id").asLong() + " " + account.get("settings").get("name").asText());
        }
    }
}
