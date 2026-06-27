package org.geysermc.generator.generator;

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
import org.geysermc.generator.mappings.FileType;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class BiomeMappingsGenerator extends MappingsGenerator<Map<Holder<Biome>, Integer>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Identifier, String> FALLBACK_BIOMES = Stream.of(
            // Different names:
            Pair.of("badlands", "mesa"),
            Pair.of("eroded_badlands", "mesa_bryce"),
            Pair.of("wooded_badlands", "mesa_plateau_stone"),
    
            Pair.of("nether_wastes", "hell"),
            Pair.of("soul_sand_valley", "soulsand_valley"),
    
            Pair.of("old_growth_birch_forest", "birch_forest_mutated"),
            Pair.of("old_growth_pine_taiga", "mega_taiga"),
            Pair.of("old_growth_spruce_taiga", "redwood_taiga_mutated"),
    
            Pair.of("snowy_beach", "cold_beach"),
            Pair.of("snowy_plains", "ice_plains"),
            Pair.of("snowy_taiga", "cold_taiga"),
    
            Pair.of("windswept_forest", "extreme_hills_plus_trees"),
            Pair.of("windswept_gravelly_hills", "extreme_hills_mutated"),
            Pair.of("windswept_hills", "extreme_hills"),
            Pair.of("windswept_savanna", "savanna_mutated"),
    
            Pair.of("dark_forest", "roofed_forest"),
            Pair.of("sparse_jungle", "jungle_edge"),
            Pair.of("ice_spikes", "ice_plains_spikes"),
            Pair.of("mushroom_fields", "mushroom_island"),
            Pair.of("swamp", "swampland"),
            Pair.of("stony_shore", "stone_beach"),
    
            // Doesn't exist on bedrock:
            Pair.of("end_barrens", "the_end"),
            Pair.of("end_highlands", "the_end"),
            Pair.of("end_midlands", "the_end"),
            Pair.of("small_end_islands", "the_end"),
            Pair.of("the_void", "river") // Not related to the end. river has similar colours.
    ).map(pair -> pair.mapFirst(Identifier::withDefaultNamespace)).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    private final CompletableFuture<RegistryAccess> registries;

    public BiomeMappingsGenerator(PackOutput output, CompletableFuture<RegistryAccess> registries) {
        super(output, FileType.BIOME_MAPPINGS);
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return registries.thenCompose(registries ->
            readExistingJsonFile(registries).thenCompose(existing ->
                readJsonFile(FileType.BIOME_ID_MAP).thenCompose(bedrockBiomes -> {
                    Map<Holder<Biome>, Integer> mappings = new Object2ObjectOpenHashMap<>(existing);
                    Registry<Biome> biomes = registries.lookupOrThrow(Registries.BIOME);

                    // Check for outdated fallback biomes
                    for (Identifier javaBiome : FALLBACK_BIOMES.keySet()) {
                        if (!biomes.containsKey(javaBiome)) {
                            LOGGER.warn("Fallback {} -> {} will never be used because the java biome doesn't actually exist.", javaBiome, FALLBACK_BIOMES.get(javaBiome));
                        }
                    }

                    for (Identifier javaBiome : biomes.keySet()) {
                        Integer bedrockId = bedrockBiomes.get(javaBiome.getPath());
                        if (bedrockId == null) {
                            String replacementBiome = FALLBACK_BIOMES.get(javaBiome);
                            if (replacementBiome != null) {
                                bedrockId = bedrockBiomes.get(replacementBiome);
                                if (bedrockId == null) {
                                    throw new IllegalStateException("Biome ID was null when explicitly replaced for " + replacementBiome);
                                }
                            } else {
                                LOGGER.warn("Replacement biome required for {}", javaBiome);
                                continue;
                            }
                        }

                        mappings.put(biomes.get(javaBiome).orElseThrow(), bedrockId);
                    }

                    return saveJsonFile(cache, registries, mappings);
                })
            )
        );
    }

    @Override
    public String getName() {
        return "Biome Mappings Generator";
    }
}
