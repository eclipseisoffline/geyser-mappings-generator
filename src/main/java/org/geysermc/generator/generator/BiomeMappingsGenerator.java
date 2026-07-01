package org.geysermc.generator.generator;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import org.geysermc.generator.mappings.FileType;
import org.geysermc.generator.names.Renamers;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class BiomeMappingsGenerator extends MappingsGenerator<Map<Holder<Biome>, Integer>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final CompletableFuture<RegistryAccess> registries;

    public BiomeMappingsGenerator(PackOutput output, CompletableFuture<RegistryAccess> registries) {
        super(output, FileType.BIOME_MAPPINGS);
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return registries.thenCompose(registries ->
            readExistingFile(registries).thenCompose(existing ->
                readFile(FileType.BIOME_ID_MAP).thenCompose(bedrockBiomes -> {
                    Map<Holder<Biome>, Integer> mappings = new Object2ObjectOpenHashMap<>(existing);
                    Registry<Biome> biomes = registries.lookupOrThrow(Registries.BIOME);

                    for (Identifier javaBiome : biomes.keySet()) {
                        String bedrockName = Renamers.BIOMES.get(javaBiome);
                        Integer bedrockId = bedrockBiomes.get(bedrockName);
                        if (bedrockId == null) {
                            LOGGER.warn("Replacement biome required for {} (bedrock biome was {}, which does not exist in palette)", javaBiome, bedrockName);
                            continue;
                        }

                        mappings.put(biomes.get(javaBiome).orElseThrow(), bedrockId);
                    }

                    return saveFile(cache, registries, mappings);
                })
            )
        );
    }

    @Override
    public String getName() {
        return "Biome Mappings Generator";
    }
}
