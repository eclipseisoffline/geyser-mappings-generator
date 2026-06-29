package org.geysermc.generator.definitions.block;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVinesPlantBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.Map;
import java.util.function.Function;

/// Java to bedrock block names
public final class BlockNames {
    private static final Map<Block, Function<BlockState, String>> nameOverrides = new Reference2ObjectOpenHashMap<>();
    
    private static void registerNameOverride(Block block, String override) {
        registerNameOverride(block, _ -> override);
    }
    
    private static void registerNameOverride(Block block, Function<BlockState, String> override) {
        if (nameOverrides.containsKey(block)) {
            throw new IllegalArgumentException("Already registered a name override for block " + block + "!");
        }
        nameOverrides.put(block, override);
    }
    
    static {
        // These don't exist on Bedrock
        registerNameOverride(Blocks.TEST_BLOCK, "unknown");
        registerNameOverride(Blocks.TEST_INSTANCE_BLOCK, "unknown");
        registerNameOverride(Blocks.VOID_AIR, "air");
        registerNameOverride(Blocks.CAVE_AIR, "air");

        registerNameOverride(Blocks.POWERED_RAIL, "golden_rail");
        registerNameOverride(Blocks.DIRT_PATH, "grass_path");
        registerNameOverride(Blocks.SMALL_DRIPLEAF, "small_dripleaf_block");
        registerNameOverride(Blocks.BIG_DRIPLEAF_STEM, "big_dripleaf");
        registerNameOverride(Blocks.FLOWERING_AZALEA_LEAVES, "azalea_leaves_flowered");
        registerNameOverride(Blocks.ROOTED_DIRT, "dirt_with_roots");
        registerNameOverride(Blocks.POWDER_SNOW_CAULDRON, "cauldron");
        registerNameOverride(Blocks.WATER_CAULDRON, "cauldron");
        registerNameOverride(Blocks.LAVA_CAULDRON, "cauldron");
        registerNameOverride(Blocks.WAXED_COPPER_BLOCK, "waxed_copper");
        registerNameOverride(Blocks.TRIPWIRE, "trip_wire");
        registerNameOverride(Blocks.MOVING_PISTON, "moving_block");
        registerNameOverride(Blocks.NOTE_BLOCK, "noteblock");
        registerNameOverride(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA, "silver_glazed_terracotta");
        registerNameOverride(Blocks.COBWEB, "web");
        registerNameOverride(Blocks.DEAD_BUSH, "deadbush");
        registerNameOverride(Blocks.TALL_SEAGRASS, "seagrass");
        registerNameOverride(Blocks.BRICKS, "brick_block");
        registerNameOverride(Blocks.WALL_TORCH, "torch");
        registerNameOverride(Blocks.SOUL_WALL_TORCH, "soul_torch");
        registerNameOverride(Blocks.COPPER_WALL_TORCH, "copper_torch");
        registerNameOverride(Blocks.SPAWNER, "mob_spawner");
        registerNameOverride(Blocks.SNOW_BLOCK, "snow");
        registerNameOverride(Blocks.SNOW, "snow_layer");
        registerNameOverride(Blocks.SUGAR_CANE, "reeds");
        registerNameOverride(Blocks.NETHER_PORTAL, "portal");
        registerNameOverride(Blocks.JACK_O_LANTERN, "lit_pumpkin");
        registerNameOverride(Blocks.MELON, "melon_block");
        registerNameOverride(Blocks.ATTACHED_PUMPKIN_STEM, "pumpkin_stem");
        registerNameOverride(Blocks.ATTACHED_MELON_STEM, "melon_stem");
        registerNameOverride(Blocks.LILY_PAD, "waterlily");
        registerNameOverride(Blocks.TERRACOTTA, "hardened_clay");
        registerNameOverride(Blocks.DARK_OAK_SIGN, "darkoak_standing_sign");
        registerNameOverride(Blocks.DARK_OAK_WALL_SIGN, "darkoak_wall_sign");
        registerNameOverride(Blocks.COBBLESTONE_STAIRS, "stone_stairs");
        registerNameOverride(Blocks.STONE_STAIRS, "normal_stone_stairs");
        registerNameOverride(Blocks.NETHER_BRICKS, "nether_brick");
        registerNameOverride(Blocks.NETHER_QUARTZ_ORE, "quartz_ore");
        registerNameOverride(Blocks.SLIME_BLOCK, "slime");
        registerNameOverride(Blocks.PRISMARINE_BRICK_STAIRS, "prismarine_bricks_stairs");
        registerNameOverride(Blocks.END_STONE_BRICKS, "end_bricks");
        registerNameOverride(Blocks.END_STONE_BRICK_STAIRS, "end_brick_stairs");
        registerNameOverride(Blocks.BEETROOTS, "beetroot");
        registerNameOverride(Blocks.MAGMA_BLOCK, "magma");
        registerNameOverride(Blocks.RED_NETHER_BRICKS, "red_nether_brick");
        registerNameOverride(Blocks.SHULKER_BOX, "undyed_shulker_box");
        registerNameOverride(Blocks.KELP_PLANT, "kelp");
        registerNameOverride(Blocks.FROGSPAWN, "frog_spawn");
        registerNameOverride(Blocks.STONECUTTER, "stonecutter_block");
        registerNameOverride(Blocks.WEEPING_VINES_PLANT, "weeping_vines");
        registerNameOverride(Blocks.TWISTING_VINES_PLANT, "twisting_vines");

        // oak -> wooden / no prefix
        registerNameOverride(Blocks.OAK_SIGN, "standing_sign");
        registerNameOverride(Blocks.OAK_WALL_SIGN, "wall_sign");
        registerNameOverride(Blocks.OAK_TRAPDOOR, "trapdoor");
        registerNameOverride(Blocks.OAK_FENCE_GATE, "fence_gate");
        registerNameOverride(Blocks.OAK_DOOR, "wooden_door");
        registerNameOverride(Blocks.OAK_PRESSURE_PLATE, "wooden_pressure_plate");
        registerNameOverride(Blocks.OAK_BUTTON, "wooden_button");

        // State-dependent overrides
        registerNameOverride(Blocks.STONE_SLAB, state -> {
            if (state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE) {
                return "normal_stone_double_slab";
            }
            return "normal_stone_slab";
        });
        registerNameOverride(Blocks.DEEPSLATE_REDSTONE_ORE, state -> {
            if (state.getValue(BlockStateProperties.LIT)) {
                return "lit_deepslate_redstone_ore";
            }
            return "deepslate_redstone_ore";
        });
        registerNameOverride(Blocks.REDSTONE_ORE, state -> {
            if (state.getValue(BlockStateProperties.LIT)) {
                return "lit_redstone_ore";
            }
            return "redstone_ore";
        });
        registerNameOverride(Blocks.REDSTONE_TORCH, state -> {
            if (state.getValue(BlockStateProperties.LIT)) {
                return "redstone_torch";
            }
            return "unlit_redstone_torch";
        });
        registerNameOverride(Blocks.REDSTONE_LAMP, state -> {
            if (state.getValue(BlockStateProperties.LIT)) {
                return "lit_redstone_lamp";
            }
            return "redstone_lamp";
        });
        registerNameOverride(Blocks.REDSTONE_WALL_TORCH, state -> {
            if (state.getValue(BlockStateProperties.LIT)) {
                return "redstone_torch";
            }
            return "unlit_redstone_torch";
        });
        registerNameOverride(Blocks.PISTON_HEAD, state -> {
            if (state.getValue(BlockStateProperties.PISTON_TYPE) == PistonType.STICKY) {
                return "sticky_piston_arm_collision";
            } else {
                return "piston_arm_collision";
            }
        });
        registerNameOverride(Blocks.REPEATER, state -> {
            if (state.getValue(BlockStateProperties.POWERED)) {
                return "powered_repeater";
            } else {
                return "unpowered_repeater";
            }
        });
        registerNameOverride(Blocks.COMPARATOR, state -> {
            if (state.getValue(BlockStateProperties.POWERED)) {
                return "powered_comparator";
            } else {
                return "unpowered_comparator";
            }
        });
        registerNameOverride(Blocks.DAYLIGHT_DETECTOR, state -> {
            if (state.getValue(BlockStateProperties.INVERTED)) {
                return "daylight_detector_inverted";
            } else {
                return "daylight_detector";
            }
        });
        registerNameOverride(Blocks.LIGHT, state -> {
            int lightValue = state.getValue(LightBlock.LEVEL);
            return "light_block_" + lightValue;
        });
        registerNameOverride(Blocks.CAVE_VINES_PLANT, state -> {
            if (state.getValue(CaveVinesPlantBlock.BERRIES)) {
                return "cave_vines_body_with_berries";
            } else {
                return "cave_vines";
            }
        });
        registerNameOverride(Blocks.CAVE_VINES, state -> {
            if (state.getValue(CaveVinesPlantBlock.BERRIES)) {
                return "cave_vines_head_with_berries";
            } else {
                return "cave_vines";
            }
        });
        registerNameOverride(Blocks.WATER, state -> {
            if (state.getValue(LiquidBlock.LEVEL) == 0) {
                return "water";
            } else {
                return "flowing_water";
            }
        });
        registerNameOverride(Blocks.LAVA, state -> {
            if (state.getValue(LiquidBlock.LEVEL) == 0) {
                return "lava";
            } else {
                return "flowing_lava";
            }
        });
        registerNameOverride(Blocks.FURNACE, state -> {
            if (state.getValue(BlockStateProperties.LIT)) {
                return "lit_furnace";
            } else {
                return "furnace";
            }
        });
        registerNameOverride(Blocks.SMOKER, state -> {
            if (state.getValue(BlockStateProperties.LIT)) {
                return "lit_smoker";
            } else {
                return "smoker";
            }
        });
        registerNameOverride(Blocks.BLAST_FURNACE, state -> {
            if (state.getValue(BlockStateProperties.LIT)) {
                return "lit_blast_furnace";
            } else {
                return "blast_furnace";
            }
        });
    }

    public static String getName(BlockState state) {
        Block block = state.getBlock();
        Function<BlockState, String> nameOverride = nameOverrides.get(block);
        if (nameOverride != null) {
            return nameOverride.apply(state);
        }

        if (block instanceof BedBlock) {
            return "bed";
        }

        if (block instanceof FlowerPotBlock) {
            return "flower_pot";
        }

        String blockName = BuiltInRegistries.BLOCK.getKey(block).getPath();

        if (block instanceof StandingSignBlock) {
            return blockName.replace("sign", "standing_sign");
        }

        if (block instanceof WallHangingSignBlock || block instanceof WallSkullBlock) {
            return blockName.replace("wall_", "");
        }

        if (block instanceof SlabBlock && state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {
            if (blockName.contains("cut_copper")) {
                return blockName.replace("cut", "double_cut");
            }
            return blockName.replace("slab", "double_slab");
        }

        if (block instanceof AbstractBannerBlock) {
            if (block instanceof WallBannerBlock) {
                return "wall_banner";
            } else {
                return "standing_banner";
            }
        }

        return blockName;
    }
    
    private BlockNames() {}
}
