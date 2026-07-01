package org.geysermc.mappings.resources;

import com.google.common.hash.Hashing;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Util;
import org.geysermc.mappings.MappingsGenerators;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public final class BedrockSamples {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path SAMPLES = Path.of("bedrock-samples.zip");

    private static final String RELEASE_INFO_BASE_URL = "https://api.github.com/repos/Mojang/bedrock-samples/releases/tags/v";

    private final Path path;

    public BedrockSamples(Path path) {
        this.path = path;
    }

    public <T> CompletableFuture<T> with(SamplesUser<T> user) {
        return CompletableFuture.supplyAsync(() -> {
            try (FileSystem fileSystem = FileSystems.newFileSystem(path)) {
                return user.use(fileSystem);
            } catch (IOException exception) {
                throw new RuntimeException("Failed to open bedrock-samples.zip", exception);
            }
        }, Util.backgroundExecutor().forName("BedrockSamples#with"));
    }

    @FunctionalInterface
    public interface SamplesUser<T> {

        T use(FileSystem samples) throws IOException;
    }

    public static CompletableFuture<BedrockSamples> load(Path root) {
        // Load bedrock version from FMJ field
        String bedrockVersion = getBedrockVersion();
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        // Try to gather GH release info of bedrock version
        return tryToGatherReleaseInfo(client, bedrockVersion).thenCompose(release -> {
            Path destination = root.resolve(SAMPLES);
            // If there's already a pack downloaded, compare hashes with GH release info
            if (Files.exists(destination)) {
                try {
                    if (sha256File(destination).equals(release.digest())) {
                        LOGGER.info("Hash for release matches local pack, not re-downloading...");
                        client.close();
                        return CompletableFuture.completedFuture(new BedrockSamples(destination));
                    }
                } catch (IOException exception) {
                    LOGGER.error("Failed to compute hash of existing bedrock-samples! Attempting to re-download...", exception);
                }
            }

            // Try to download if there is no pack or the hashes don't line up
            return tryToDownloadSamples(client, release, destination).thenCompose(_ -> {
                client.close();

                // Now we compare hashes again and fail if they (still) don't line up
                String sha256;
                try {
                    sha256 = sha256File(destination);
                } catch (IOException exception) {
                    throw new RuntimeException("Failed to compute hash of downloaded bedrock-samples!", exception);
                }
                if (sha256.equals(release.digest)) {
                    return CompletableFuture.completedFuture(new BedrockSamples(destination));
                }
                throw new IllegalStateException("Hashes of downloaded bedrock-samples and GitHub don't line up!\n" +
                        "GitHub: " + release.digest + "\n" +
                        "Local: " + sha256);
            });
        });
    }

    private static CompletableFuture<ReleaseInfo> tryToGatherReleaseInfo(HttpClient client, String bedrockVersion) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(RELEASE_INFO_BASE_URL + bedrockVersion)).build();
        LOGGER.info("Gathering release info for bedrock-samples version " + bedrockVersion);
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Received status code " + response.statusCode() + " when trying to gather release info for bedrock version " + bedrockVersion);
            }
            try {
                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray assets = json.getAsJsonArray("assets");

                for (JsonElement asset : assets) {
                    String name = asset.getAsJsonObject().get("name").getAsString();
                    if (name.endsWith("-min.zip")) {
                        return new ReleaseInfo(URI.create(asset.getAsJsonObject().get("browser_download_url").getAsString()),
                                asset.getAsJsonObject().get("digest").getAsString().replaceFirst("sha256:", ""), bedrockVersion);
                    }
                }
                throw new IllegalStateException("Unable to find asset that ends with -min.zip");
            } catch (Exception exception) {
                throw new RuntimeException("Caught exception when trying to gather release info for bedrock version " + bedrockVersion, exception);
            }
        });
    }

    private static CompletableFuture<Path> tryToDownloadSamples(HttpClient client, ReleaseInfo release, Path destination) {
        HttpRequest request = HttpRequest.newBuilder(release.uri).build();
        LOGGER.info("Downloading bedrock-samples version {}", release.version);
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofFile(destination)).thenApply(response -> {
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Received status code " + response.statusCode() + " when trying to download bedrock-samples for bedrock version " + release.version);
            }
            return response.body();
        });
    }

    private static String sha256File(Path file) throws IOException {
        return Hashing.sha256().hashBytes(Files.readAllBytes(file)).toString();
    }

    private record ReleaseInfo(URI uri, String digest, String version) {}

    private static String getBedrockVersion() {
        return FabricLoader.getInstance().getModContainer(MappingsGenerators.MOD_ID)
                .map(container -> container.getMetadata().getCustomValue("geyser:bedrock_version").getAsString())
                .orElseThrow();
    }
}
