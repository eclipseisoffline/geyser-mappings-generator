package org.geysermc.mappings.generator;

import com.mojang.datafixers.util.Pair;
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
import org.geysermc.mappings.FileType;
import org.geysermc.mappings.definitions.biome.BedrockBiome;
import org.geysermc.mappings.names.Renamers;
import org.geysermc.mappings.resources.BedrockSamples;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;

public final class BiomeMappingsGenerator extends MappingsGenerator<Map<Holder<Biome>, Integer>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final CompletableFuture<RegistryAccess> registries;
    private final CompletableFuture<BedrockSamples> bedrockSamples;

    public BiomeMappingsGenerator(PackOutput output, CompletableFuture<RegistryAccess> registries, CompletableFuture<BedrockSamples> bedrockSamples) {
        super(output, FileType.BIOME_MAPPINGS);
        this.registries = registries;
        this.bedrockSamples = bedrockSamples;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        CompletableFuture<List<BedrockBiome>> bedrockBiomesFuture = bedrockSamples.thenCompose(samples -> samples.with(access -> access.readFileOrThrow(FileType.BEDROCK_BIOMES)));

        return registries.thenCompose(registries ->
            readExistingFile(registries).thenCombine(bedrockBiomesFuture, Pair::of).thenCompose(mappingsAndBiomes -> {
                Registry<Biome> biomes = registries.lookupOrThrow(Registries.BIOME);

                Map<Holder<Biome>, Integer> mappings = new Object2ObjectOpenHashMap<>();
                mappingsAndBiomes.getFirst().ifPresent(mappings::putAll);
                List<BedrockBiome> bedrockBiomes = mappingsAndBiomes.getSecond();

                for (Identifier javaBiome : biomes.keySet()) {
                    Identifier bedrockName = Renamers.BIOMES.get(javaBiome);
                    OptionalInt bedrockId = bedrockBiomes.stream()
                            .filter(biome -> biome.name().equals(bedrockName))
                            .mapToInt(BedrockBiome::id)
                            .findFirst();
                    if (bedrockId.isEmpty()) {
                        LOGGER.error("Replacement biome required for {} (bedrock biome was {}, which does not exist in palette)", javaBiome, bedrockName);
                        continue;
                    }

                    mappings.put(biomes.get(javaBiome).orElseThrow(), bedrockId.getAsInt());
                }

                return saveFile(cache, registries, mappings);
            })
        );
    }

    @Override
    public String getName() {
        return "Biome Mappings Generator";
    }
}
