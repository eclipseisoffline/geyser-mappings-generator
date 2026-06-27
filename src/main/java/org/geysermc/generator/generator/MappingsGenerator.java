package org.geysermc.generator.generator;

import com.mojang.serialization.JsonOps;
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
