package org.geysermc.generator.generator;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.serialization.JavaOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.util.Util;
import org.geysermc.generator.mappings.FileType;
import org.geysermc.generator.mappings.MappingAccess;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public abstract class MappingsGenerator<T> implements DataProvider, MappingAccess {
    protected final PackOutput packOutput;
    protected final FileType<T> type;
    protected final Path output;

    public MappingsGenerator(PackOutput output, FileType<T> type) {
        this.packOutput = output;
        this.type = type;
        this.output = path(type);
    }

    // Inspired by DataProvider#saveStable
    protected CompletableFuture<?> saveTextFile(CachedOutput cache, T value) {
        return CompletableFuture.runAsync(() -> {
            try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
                try (HashingOutputStream hashedBytes = new HashingOutputStream(Hashing.sha1(), bytes)) {
                    hashedBytes.write(type.codec().encodeStart(JavaOps.INSTANCE, value).getOrThrow().toString().getBytes(StandardCharsets.UTF_8));
                    cache.writeIfNeeded(output, bytes.toByteArray(), hashedBytes.hash());
                }
            } catch (IOException exception) {
                LOGGER.error("Failed to save file to {}", output, exception);
            }
        }, Util.backgroundExecutor().forName("saveTextFile"));
    }

    protected CompletableFuture<?> saveJsonFile(CachedOutput cache, T value) {
        return DataProvider.saveStable(cache, type.codec(), value, output);
    }

    protected CompletableFuture<?> saveJsonFile(CachedOutput cache, RegistryAccess registries, T value) {
        return DataProvider.saveStable(cache, registries, type.codec(), value, output);
    }

    protected CompletableFuture<T> readExistingJsonFile() {
        return readJsonFile(type);
    }

    protected CompletableFuture<T> readExistingJsonFile(RegistryAccess registries) {
        return readJsonFile(type, registries);
    }

    @Override
    public Path mappingsFolder() {
        return packOutput.getOutputFolder();
    }
}
