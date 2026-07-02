package org.geysermc.mappings.generator;

import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.geysermc.mappings.FileType;
import org.geysermc.mappings.MappingsAccess;

import java.nio.file.Path;
import java.util.Optional;
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

    protected CompletableFuture<Optional<T>> readExistingFile() {
        return readFile(type);
    }

    protected CompletableFuture<Optional<T>> readExistingFile(RegistryAccess registries) {
        return readFile(type, registries);
    }

    @Override
    public final Path mappingsFolder() {
        return packOutput.getOutputFolder();
    }
}
