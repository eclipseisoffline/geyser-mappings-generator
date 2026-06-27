package org.geysermc.generator.generator;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.geysermc.generator.mappings.FileType;
import org.geysermc.generator.mappings.MappingAccess;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public abstract class JsonMappingsGenerator<T> implements DataProvider, MappingAccess {
    protected final PackOutput packOutput;
    protected final FileType<T> type;
    protected final Path output;

    public JsonMappingsGenerator(PackOutput output, FileType<T> type) {
        this.packOutput = output;
        this.type = type;
        this.output = path(type);
    }

    protected CompletableFuture<?> saveFile(CachedOutput cache, T value) {
        return DataProvider.saveStable(cache, type.codec(), value, output);
    }

    protected CompletableFuture<T> readExistingFile() {
        return readFile(type);
    }

    @Override
    public Path mappingsFolder() {
        return packOutput.getOutputFolder();
    }
}
