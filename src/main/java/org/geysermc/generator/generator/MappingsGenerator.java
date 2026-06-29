package org.geysermc.generator.generator;

import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.geysermc.generator.mappings.FileType;
import org.geysermc.generator.mappings.MappingsAccess;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public abstract class MappingsGenerator<T> implements DataProvider, MappingsAccess {
    protected final PackOutput packOutput;
    protected final FileType<T> type;
    protected final Path output;

    public MappingsGenerator(PackOutput output, FileType<T> type) {
        this.packOutput = output;
        this.type = type;
        this.output = path(type);
    }

    protected CompletableFuture<?> saveFile(CachedOutput cache, T value) {
        return saveFile(cache, type, value);
    }

    protected CompletableFuture<?> saveFile(CachedOutput cache, RegistryAccess registries, T value) {
        return saveFile(cache, registries, type, value);
    }

    protected CompletableFuture<T> readExistingFile() {
        return readFile(type);
    }

    protected CompletableFuture<T> readExistingFile(RegistryAccess registries) {
        return readFile(type, registries);
    }

    @Override
    public Path mappingsFolder() {
        return packOutput.getOutputFolder();
    }
}
