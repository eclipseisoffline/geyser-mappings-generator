package org.geysermc.mappings.generator;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import org.geysermc.mappings.definitions.util.UtilMappings;
import org.geysermc.mappings.FileType;

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
