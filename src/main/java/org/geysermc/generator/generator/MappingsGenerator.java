package org.geysermc.generator.generator;

import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.geysermc.generator.mappings.FileType;
import org.geysermc.generator.mappings.MappingAccess;

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

    protected CompletableFuture<?> saveTextFile(CachedOutput cache, T value) {
        return saveTextFile(cache, type, value);
    }

    protected CompletableFuture<?> saveNbtFile(CachedOutput cache, T value) {
        return saveNbtFile(cache, type, value);
    }

    protected CompletableFuture<?> saveNbtFile(CachedOutput cache, RegistryAccess registries, T value) {
        return saveNbtFile(cache, registries, type, value);
    }

    protected CompletableFuture<?> saveJsonFile(CachedOutput cache, T value) {
        return saveJsonFile(cache, type, value);
    }

    protected CompletableFuture<?> saveJsonFile(CachedOutput cache, RegistryAccess registries, T value) {
        return saveJsonFile(cache, registries, type, value);
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
