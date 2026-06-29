package org.geysermc.generator.generator;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import org.geysermc.generator.definitions.util.UtilMappings;
import org.geysermc.generator.mappings.FileType;

import java.util.concurrent.CompletableFuture;

public final class UtilMappingsGenerator extends MappingsGenerator<UtilMappings> {

    public UtilMappingsGenerator(PackOutput output) {
        super(output, FileType.UTIL_MAPPINGS);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return saveFile(cache, UtilMappings.create());
    }

    @Override
    public String getName() {
        return "Util Mappings Generator";
    }
}
