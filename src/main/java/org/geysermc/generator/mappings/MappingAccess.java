package org.geysermc.generator.mappings;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Util;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface MappingAccess {

    Logger LOGGER = LogUtils.getLogger();

    default <T> CompletableFuture<?> saveTextFile(CachedOutput cache, FileType<T> type, T value) {
        return saveTextFile(cache, path(type), type.codec(), value);
    }

    default <T> CompletableFuture<?> saveNbtFile(CachedOutput cache, FileType<T> type, T value) {
        return saveNbtFile(cache, path(type), type.codec(), value);
    }

    default <T> CompletableFuture<?> saveNbtFile(CachedOutput cache, RegistryAccess registries, FileType<T> type, T value) {
        return saveNbtFile(cache, path(type), registries, type.codec(), value);
    }

    default <T> CompletableFuture<?> saveJsonFile(CachedOutput cache, FileType<T> type, T value) {
        return saveJsonFile(cache, path(type), type.codec(), value);
    }

    default <T> CompletableFuture<?> saveJsonFile(CachedOutput cache, RegistryAccess registries, FileType<T> type, T value) {
        return saveJsonFile(cache, path(type), registries, type.codec(), value);
    }

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

    static <T> CompletableFuture<?> saveTextFile(CachedOutput output, Path file, Codec<T> codec, T value) {
        return saveFile(output, file, (object, stream) -> stream.write(object.toString().getBytes(StandardCharsets.UTF_8)), JavaOps.INSTANCE, codec, value);
    }

    static <T> CompletableFuture<?> saveNbtFile(CachedOutput output, Path file, Codec<T> codec, T value) {
        return saveNbtFile(output, file, NbtOps.INSTANCE, codec, value);
    }

    static <T> CompletableFuture<?> saveNbtFile(CachedOutput output, Path file, RegistryAccess registries, Codec<T> codec, T value) {
        return saveNbtFile(output, file, registries.createSerializationContext(NbtOps.INSTANCE), codec, value);
    }

    static <T> CompletableFuture<?> saveNbtFile(CachedOutput output, Path file, DynamicOps<Tag> ops, Codec<T> codec, T value) {
        return saveFile(output, file, (tag, stream) -> NbtIo.writeCompressed((CompoundTag) tag, stream), ops, codec, value);
    }

    static <T> CompletableFuture<T> readNbtFile(Path file, Codec<T> codec) {
        return readNbtFile(file, NbtOps.INSTANCE, codec);
    }

    static <T> CompletableFuture<T> readNbtFile(Path file, DynamicOps<Tag> ops, Codec<T> codec) {
        return readFile(() -> NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap()), ops, codec);
    }

    static <T> CompletableFuture<?> saveJsonFile(CachedOutput output, Path file, Codec<T> codec, T value) {
        return saveJsonFile(output, file, JsonOps.INSTANCE, codec, value);
    }

    static <T> CompletableFuture<?> saveJsonFile(CachedOutput output, Path file, RegistryAccess registries, Codec<T> codec, T value) {
        return saveJsonFile(output, file, registries.createSerializationContext(JsonOps.INSTANCE), codec, value);
    }

    // Inspired by DataProvider#saveStable
    static <T> CompletableFuture<?> saveJsonFile(CachedOutput output, Path file, DynamicOps<JsonElement> ops, Codec<T> codec, T value) {
        return saveFile(output, file, (json, stream) -> {
            try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8))) {
                writer.setSerializeNulls(false);
                writer.setIndent("  ");
                GsonHelper.writeValue(writer, json, DataProvider.KEY_COMPARATOR);
            }
        }, ops, codec, value);
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

    // Inspired by DataProvider#saveStable
    static <O, T> CompletableFuture<?> saveFile(CachedOutput cache, Path path, IOWriter<O> writer, DynamicOps<O> ops, Codec<T> codec, T value) {
        return CompletableFuture.runAsync(() -> {
            try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
                try (HashingOutputStream hashedBytes = new HashingOutputStream(Hashing.sha1(), bytes)) {
                    writer.write(codec.encodeStart(ops, value).getOrThrow(), hashedBytes);
                    cache.writeIfNeeded(path, bytes.toByteArray(), hashedBytes.hash());
                }
            } catch (IOException exception) {
                LOGGER.error("Failed to save file to {}", path, exception);
            }
        }, Util.backgroundExecutor().forName("MappingAccess#saveFile"));
    }

    static <O, T> CompletableFuture<T> readFile(IOReader<O> reader, DynamicOps<O> ops, Codec<T> codec) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return codec.parse(ops, reader.read()).getOrThrow();
            } catch (IOException exception) {
                throw new RuntimeException("IOException occurred whilst reading file", exception);
            }
        }, Util.backgroundExecutor().forName("MappingAccess#readFile"));
    }

    @FunctionalInterface
    interface IOReader<T> {

        T read() throws IOException;
    }

    @FunctionalInterface
    interface IOWriter<T> {

        void write(T value, OutputStream stream) throws IOException;
    }
}
