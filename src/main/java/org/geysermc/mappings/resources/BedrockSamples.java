package org.geysermc.mappings.resources;

import com.google.common.hash.Hashing;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Util;
import org.geysermc.mappings.FileSystemAccess;
import org.geysermc.mappings.MappingsGenerators;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.zip.ZipFile;

public final class BedrockSamples {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path DATA = Path.of("bedrock-data.zip");
    private static final Path SAMPLES = Path.of("bedrock-samples.zip");

    private static final String DATA_DOWNLOAD_BASE_URL = "http://api.github.com/repos/cloudburstmc/data/zipball/";
    private static final String SAMPLES_RELEASE_INFO_BASE_URL = "https://api.github.com/repos/Mojang/bedrock-samples/releases/tags/v";

    private final Path dataPath;
    private final Path samplesPath;

    public BedrockSamples(Path dataPath, Path samplesPath) {
        this.dataPath = dataPath;
        this.samplesPath = samplesPath;
    }

    private <T> CompletableFuture<T> openFileSystemRaw(Path path, RawFileSystemUser<T> user) {
        return CompletableFuture.supplyAsync(() -> {
            try (FileSystem fileSystem = FileSystems.newFileSystem(path)) {
                return user.use(fileSystem);
            } catch (IOException exception) {
                throw new UncheckedIOException("Failed to open " + path.getFileName().toString(), exception);
            }
        }, Util.backgroundExecutor().forName("BedrockSamples#open"));
    }

    public <T> CompletableFuture<T> openSamplesRaw(RawFileSystemUser<T> user) {
        return openFileSystemRaw(samplesPath, user);
    }

    private <T> CompletableFuture<T> openFileSystem(Path path, Function<FileSystem, Path> rootGetter, FileSystemUser<T> user) {
        return openFileSystemRaw(path, system -> {
            Path root = rootGetter.apply(system);
            return user.use(() -> root).join();
        });
    }

    public <T> CompletableFuture<T> openData(FileSystemUser<T> user) {
        return openFileSystem(dataPath, system -> {
            // Have to do this because GitHub doesn't put the files in the root of the ZIP, rather it creates a folder with the name of the repo and the commit SHA
            // and puts the files in there
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(system.getPath("/"))) {
                return stream.iterator().next();
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }, user);
    }

    public <T> CompletableFuture<T> openSamples(FileSystemUser<T> user) {
        return openFileSystem(samplesPath, system -> system.getPath("/"), user);
    }

    @FunctionalInterface
    public interface RawFileSystemUser<T> {

        T use(FileSystem samples) throws IOException;
    }

    @FunctionalInterface
    public interface FileSystemUser<T> {

        CompletableFuture<T> use(FileSystemAccess access) throws IOException;
    }

    public static CompletableFuture<BedrockSamples> load(Path root) {
        // Load bedrock version from FMJ fields
        String bedrockVersion = getBedrockVersion();
        String bedrockSha = getBedrockDataSha();

        // Try to load both data and samples asynchronously
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        CompletableFuture<Path> data = tryToLoadBedrockData(client, bedrockSha, root);
        CompletableFuture<Path> samples = tryToLoadBedrockSamples(client, bedrockVersion, root);
        return CompletableFuture.allOf(data, samples).thenApply(_ -> new BedrockSamples(data.join(), samples.join()));
    }

    private static CompletableFuture<Path> tryToLoadBedrockData(HttpClient client, String bedrockSha, Path root) {
        Path destination = root.resolve(DATA);
        // If there's already data downloaded, compare commit hash with the zip's comment (GitHub puts the sha there)
        if (Files.exists(destination)) {
            try {
                if (bedrockSha.equals(zipComment(destination))) {
                    LOGGER.info("bedrock-data commit hash matches local download, not re-downloading");
                    return CompletableFuture.completedFuture(destination);
                }
                Files.delete(destination);
            } catch (IOException exception) {
                LOGGER.error("Failed to read commit hash of existing bedrock-data! Attempting to re-download...", exception);
            }
        }

        // Try to download if there is no existing data or the commit hash doesn't line up
        return tryToDownloadData(client, bedrockSha, destination);
    }

    private static CompletableFuture<Path> tryToLoadBedrockSamples(HttpClient client, String bedrockVersion, Path root) {
        // Try to gather GH release info of bedrock version
        return tryToGatherSamplesReleaseInfo(client, bedrockVersion).thenCompose(release -> {
            Path destination = root.resolve(SAMPLES);
            // If there's already a pack downloaded, compare hashes with GH release info
            if (Files.exists(destination)) {
                try {
                    if (sha256File(destination).equals(release.digest())) {
                        LOGGER.info("bedrock-samples hash for release matches local pack, not re-downloading...");
                        client.close();
                        return CompletableFuture.completedFuture(destination);
                    }
                    Files.delete(destination);
                } catch (IOException exception) {
                    LOGGER.error("Failed to compute hash of existing bedrock-samples! Attempting to re-download...", exception);
                }
            }

            // Try to download if there is no pack or the hashes don't line up
            return tryToDownloadSamples(client, release, destination).thenApply(_ -> {
                client.close();

                // Now we compare hashes again and fail if they (still) don't line up
                String sha256;
                try {
                    sha256 = sha256File(destination);
                } catch (IOException exception) {
                    throw new UncheckedIOException("Failed to compute hash of downloaded bedrock-samples!", exception);
                }
                if (sha256.equals(release.digest)) {
                    return destination;
                }
                throw new IllegalStateException("Hashes of downloaded bedrock-samples and GitHub don't line up!\n" +
                        "GitHub: " + release.digest + "\n" +
                        "Local: " + sha256);
            });
        });
    }

    private static CompletableFuture<ReleaseInfo> tryToGatherSamplesReleaseInfo(HttpClient client, String bedrockVersion) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(SAMPLES_RELEASE_INFO_BASE_URL + bedrockVersion)).build();
        LOGGER.info("Gathering release info for bedrock-samples version {}", bedrockVersion);
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(response -> {
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Received status code " + response.statusCode() + " when trying to gather bedrock-samples release info for bedrock version " + bedrockVersion);
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
                throw new RuntimeException("Caught exception when trying to gather bedrock-samples release info for bedrock version " + bedrockVersion, exception);
            }
        });
    }

    private static CompletableFuture<Path> tryToDownloadData(HttpClient client, String sha, Path destination) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(DATA_DOWNLOAD_BASE_URL + sha)).build();
        LOGGER.info("Downloading bedrock-data for commit {}", sha);
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofFile(destination)).thenApply(response -> {
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Received status code " + response.statusCode() + " when trying to download bedrock-data for commit " + sha);
            }
            return response.body();
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

    private static @Nullable String zipComment(Path file) throws IOException {
        try (ZipFile zip = new ZipFile(file.toFile())) {
            return zip.getComment();
        }
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

    private static String getBedrockDataSha() {
        return FabricLoader.getInstance().getModContainer(MappingsGenerators.MOD_ID)
                .map(container -> container.getMetadata().getCustomValue("geyser:bedrock_data_sha").getAsString())
                .orElseThrow();
    }
}
