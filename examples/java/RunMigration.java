// Requires Java 11+ and jackson-databind

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RunMigration {

    static final String API_URL = "https://api.flexify.io";
    static final String API_TOKEN = "<your API token>"; // replace with your API token

    // Replace with your storage account IDs (see ListStorageAccounts.java) and bucket names
    static final long SOURCE_STORAGE_ACCOUNT_ID = 1;
    static final String SOURCE_BUCKET = "source-bucket";
    static final long DESTINATION_STORAGE_ACCOUNT_ID = 2;
    static final String DESTINATION_BUCKET = "destination-bucket";

    static final ObjectMapper JSON = new ObjectMapper();
    static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(60)).build();

    public static void main(String[] args) throws Exception {
        ObjectNode migration = JSON.createObjectNode();

        ObjectNode settings = migration.putObject("settings");
        settings.put("name", "API example");
        settings.put("migrationMode", "COPY"); // or MOVE
        settings.put("conflictResolution", "NEWER"); // or SOURCE, DESTINATION
        settings.put("existingDataInDestination", "KEEP"); // or CLEAN
        settings.putNull("comparisonMethod"); // or AUTO, PROBE_ONLY, LIST_ONLY, LIST_PROBE
        settings.put("skipIfHashMatches", true);
        settings.put("strictConsistencyCheck", false);
        settings.put("verifyBuckets", true);
        settings.put("dryRun", false);

        // Engines
        settings.put("enginesSelectionMethod", "AUTO"); // or SHARED, EXISTING, DEPLOYMENT
        // GET /cloud-locations/existing returns the locations that have engines
        settings.putNull("enginesLocation"); // or settings.putObject("enginesLocation").put("cloudCode", "AWS").put("region", "us-east-1").put("netConfig", "flexify-direct");
        settings.putNull("maxEngines"); // or a number
        settings.putNull("maxStreams"); // or a number
        settings.putNull("slotsPerMapping"); // or a number

        // Which objects to migrate
        settings.putNull("objectKeyFilter"); // or a pattern such as "photos/*"
        settings.putNull("lastModifiedFrom"); // or a date such as "2026-01-01T00:00:00Z"
        settings.putNull("lastModifiedTo"); // or a date such as "2026-12-31T23:59:59Z"

        // Limits
        settings.putNull("maxBytesPerSecond"); // or a number
        settings.putNull("maxRequestsPerSecond"); // or a number

        // Multipart upload
        settings.putNull("multipartLimit"); // or the minimum object size in bytes
        settings.putNull("multipartPartSize"); // or the part size in bytes
        settings.putNull("multipartConcurrency"); // or the number of parts uploaded in parallel

        // Retries
        settings.put("maxRetries", 100);
        settings.put("maxRetriesForCopy", 10);
        settings.put("retryTimeout", 5); // seconds
        settings.put("maxRetryTimeout", 60); // seconds

        // Objects in an archival tier
        settings.put("autoRestoreIfArchived", false);
        settings.putNull("restoreTier"); // or BULK, STANDARD, EXPEDITED
        settings.putNull("restoreDays"); // or the number of days to keep the restored objects
        settings.putNull("restoreMaxSize"); // or the maximum total size in bytes

        // Other
        settings.putNull("removeHeaders"); // or a list of header names
        settings.putNull("uploadTimestampMode"); // for Backblaze B2: ACTUAL, ORIGINAL or CUSTOM
        settings.putNull("uploadTimestampValue"); // for CUSTOM: a date such as "2026-01-01T00:00:00Z"
        settings.putNull("migrationReportLevel"); // or ALL, NOT_SKIPPED, FAILED, NONE

        ObjectNode mapping = migration.putArray("mappings").addObject();
        mapping.put("sourceStorageAccountId", SOURCE_STORAGE_ACCOUNT_ID);
        mapping.put("sourceBucketName", SOURCE_BUCKET);
        mapping.put("destStorageAccountId", DESTINATION_STORAGE_ACCOUNT_ID);
        mapping.put("destBucketName", DESTINATION_BUCKET);
        mapping.putNull("destBucketNewRegion"); // or the region to create the destination bucket in
        mapping.putNull("keyRemovePrefix"); // or a prefix to remove from each object key
        mapping.putNull("keyAddPrefix"); // or a prefix to add to each object key
        mapping.putNull("objectsListUri"); // or the URI of a text file with the objects to migrate
        mapping.putNull("cdnUrl"); // or the base URL of a CDN to read the source objects through

        long migrationId = send(request("/migrations")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(migration.toString())))
                .get("id").asLong();
        System.out.println("Started migration " + migrationId);

        String state;
        while (true) {
            JsonNode stat = send(request("/migrations/" + migrationId)).get("stat");
            state = stat.get("state").asText();
            System.out.println(state + " " + Math.round(stat.path("progress").asDouble() * 100) + "%");
            if (Set.of("SUCCEEDED", "FAILED", "STOPPED").contains(state)) {
                break;
            }
            Thread.sleep(10_000);
        }

        if (!state.equals("SUCCEEDED")) {
            System.exit(1);
        }
    }

    static HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(API_URL + path))
                .header("Authorization", "Bearer " + API_TOKEN)
                .timeout(Duration.ofSeconds(60));
    }

    static JsonNode send(HttpRequest.Builder request) throws Exception {
        HttpResponse<String> response = HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        }
        return JSON.readTree(response.body());
    }
}
