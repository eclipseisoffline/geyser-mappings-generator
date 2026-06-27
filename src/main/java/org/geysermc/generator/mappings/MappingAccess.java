package org.geysermc.generator.mappings;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface MappingAccess {

    default <T> CompletableFuture<T> readFile(FileType<T> type) {
        return readFile(path(type), type.codec());
    }

    default Path path(FileType<?> type) {
        return mappingsFolder().resolve(type.path());
    }

    Path mappingsFolder();

    static <T> CompletableFuture<T> readFile(Path file, Codec<T> codec) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return codec.parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file))).getOrThrow();
            } catch (IOException exception) {
                throw new RuntimeException("IOException occurred whilst reading file", exception);
            }
        }, Util.backgroundExecutor().forName("JsonMappingsGenerator#readFile"));
    }
}
