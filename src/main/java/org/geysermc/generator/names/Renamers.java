package org.geysermc.generator.names;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.geysermc.generator.names.renamers.BlockNames;
import org.geysermc.generator.names.renamers.ItemNames;

public final class Renamers {
    public static final TypeRenamer<Identifier, String> BIOMES = TypeRenamer.of(Identifier::getPath, builder -> builder
            .<ResourceKey<Biome>>mapType(ResourceKey::identifier)
            // Different names:
            .rename(Biomes.BADLANDS, "mesa")
            .rename(Biomes.ERODED_BADLANDS, "mesa_bryce")
            .rename(Biomes.WOODED_BADLANDS, "mesa_plateau_stone")

            .rename(Biomes.NETHER_WASTES, "hell")
            .rename(Biomes.SOUL_SAND_VALLEY, "soulsand_valley")

            .rename(Biomes.OLD_GROWTH_BIRCH_FOREST, "birch_forest_mutated")
            .rename(Biomes.OLD_GROWTH_PINE_TAIGA, "mega_taiga")
            .rename(Biomes.OLD_GROWTH_SPRUCE_TAIGA, "redwood_taiga_mutated")

            .rename(Biomes.SNOWY_BEACH, "cold_beach")
            .rename(Biomes.SNOWY_PLAINS, "ice_plains")
            .rename(Biomes.SNOWY_TAIGA, "cold_taiga")

            .rename(Biomes.WINDSWEPT_FOREST, "extreme_hills_plus_trees")
            .rename(Biomes.WINDSWEPT_GRAVELLY_HILLS, "extreme_hills_mutated")
            .rename(Biomes.WINDSWEPT_HILLS, "extreme_hills")
            .rename(Biomes.WINDSWEPT_SAVANNA, "savanna_mutated")

            .rename(Biomes.DARK_FOREST, "roofed_forest")
            .rename(Biomes.SPARSE_JUNGLE, "jungle_edge")
            .rename(Biomes.ICE_SPIKES, "ice_plains_spikes")
            .rename(Biomes.MUSHROOM_FIELDS, "mushroom_island")
            .rename(Biomes.SWAMP, "swampland")
            .rename(Biomes.STONY_SHORE, "stone_beach")

            // Doesn't exist on bedrock:
            .rename(Biomes.END_BARRENS, "the_end")
            .rename(Biomes.END_HIGHLANDS, "the_end")
            .rename(Biomes.END_MIDLANDS, "the_end")
            .rename(Biomes.SMALL_END_ISLANDS, "the_end")
            .rename(Biomes.THE_VOID, "river")); // Not related to the end. river has similar colours.
    public static final InstanceRenamer<Block, BlockState, String> BLOCKS = BlockNames.INSTANCE;
    public static final InstanceRenamer<Item, Identifier, Identifier> ITEMS = ItemNames.INSTANCE;

    private Renamers() {}
}
