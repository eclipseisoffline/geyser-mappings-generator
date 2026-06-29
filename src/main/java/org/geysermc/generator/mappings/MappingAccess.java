package org.geysermc.generator.mappings;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface MappingAccess {

    default <T> CompletableFuture<T> readNbtFile(FileType<T> type) {
        return readNbtFile(path(type), type.codec());
    }

    default <T> CompletableFuture<T> readJsonFile(FileType<T> type) {
        return readJsonFile(path(type), type.codec());
    }

    default <T> CompletableFuture<T> readJsonFile(FileType<T> type, RegistryAccess registries) {
        return readJsonFile(path(type), registries, type.codec());
    }

    default Path path(FileType<?> type) {
        return mappingsFolder().resolve(type.path());
    }

    Path mappingsFolder();

    static <T> CompletableFuture<T> readNbtFile(Path file, Codec<T> codec) {
        return readNbtFile(file, NbtOps.INSTANCE, codec);
    }

    static <T> CompletableFuture<T> readNbtFile(Path file, DynamicOps<Tag> ops, Codec<T> codec) {
        return readFile(() -> NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap()), ops, codec);
    }

    static <T> CompletableFuture<T> readJsonFile(Path file, Codec<T> codec) {
        return readJsonFile(file, JsonOps.INSTANCE, codec);
    }

    static <T> CompletableFuture<T> readJsonFile(Path file, RegistryAccess registries, Codec<T> codec) {
        return readJsonFile(file, registries.createSerializationContext(JsonOps.INSTANCE), codec);
    }

    static <T> CompletableFuture<T> readJsonFile(Path file, DynamicOps<JsonElement> ops, Codec<T> codec) {
        return readFile(() -> JsonParser.parseString(Files.readString(file)), ops, codec);
    }

    static <O, T> CompletableFuture<T> readFile(IOSupplier<O> supplier, DynamicOps<O> ops, Codec<T> codec) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return codec.parse(ops, supplier.get()).getOrThrow();
            } catch (IOException exception) {
                throw new RuntimeException("IOException occurred whilst reading file", exception);
            }
        }, Util.backgroundExecutor().forName("JsonMappingsGenerator#readFile"));
    }

    @FunctionalInterface
    interface IOSupplier<T> {

        T get() throws IOException;
    }
}
