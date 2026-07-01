package org.geysermc.mappings.names.renamers;

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
import org.geysermc.mappings.names.InstanceRenamer;

public final class BlockNames {
    public static final InstanceRenamer<Block, BlockState, String> INSTANCE = InstanceRenamer.of(BlockNames::mapName, builder -> builder
            // These don't exist on Bedrock
            .rename(Blocks.TEST_BLOCK, "unknown")
            .rename(Blocks.TEST_INSTANCE_BLOCK, "unknown")
            .rename(Blocks.VOID_AIR, "air")
            .rename(Blocks.CAVE_AIR, "air")

            // Named differently
            .rename(Blocks.POWERED_RAIL, "golden_rail")
            .rename(Blocks.DIRT_PATH, "grass_path")
            .rename(Blocks.SMALL_DRIPLEAF, "small_dripleaf_block")
            .rename(Blocks.BIG_DRIPLEAF_STEM, "big_dripleaf")
            .rename(Blocks.FLOWERING_AZALEA_LEAVES, "azalea_leaves_flowered")
            .rename(Blocks.ROOTED_DIRT, "dirt_with_roots")
            .rename(Blocks.POWDER_SNOW_CAULDRON, "cauldron")
            .rename(Blocks.WATER_CAULDRON, "cauldron")
            .rename(Blocks.LAVA_CAULDRON, "cauldron")
            .rename(Blocks.WAXED_COPPER_BLOCK, "waxed_copper")
            .rename(Blocks.TRIPWIRE, "trip_wire")
            .rename(Blocks.MOVING_PISTON, "moving_block")
            .rename(Blocks.NOTE_BLOCK, "noteblock")
            .rename(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA, "silver_glazed_terracotta")
            .rename(Blocks.COBWEB, "web")
            .rename(Blocks.DEAD_BUSH, "deadbush")
            .rename(Blocks.TALL_SEAGRASS, "seagrass")
            .rename(Blocks.BRICKS, "brick_block")
            .rename(Blocks.WALL_TORCH, "torch")
            .rename(Blocks.SOUL_WALL_TORCH, "soul_torch")
            .rename(Blocks.COPPER_WALL_TORCH, "copper_torch")
            .rename(Blocks.SPAWNER, "mob_spawner")
            .rename(Blocks.SNOW_BLOCK, "snow")
            .rename(Blocks.SNOW, "snow_layer")
            .rename(Blocks.SUGAR_CANE, "reeds")
            .rename(Blocks.NETHER_PORTAL, "portal")
            .rename(Blocks.JACK_O_LANTERN, "lit_pumpkin")
            .rename(Blocks.MELON, "melon_block")
            .rename(Blocks.ATTACHED_PUMPKIN_STEM, "pumpkin_stem")
            .rename(Blocks.ATTACHED_MELON_STEM, "melon_stem")
            .rename(Blocks.LILY_PAD, "waterlily")
            .rename(Blocks.TERRACOTTA, "hardened_clay")
            .rename(Blocks.DARK_OAK_SIGN, "darkoak_standing_sign")
            .rename(Blocks.DARK_OAK_WALL_SIGN, "darkoak_wall_sign")
            .rename(Blocks.COBBLESTONE_STAIRS, "stone_stairs")
            .rename(Blocks.STONE_STAIRS, "normal_stone_stairs")
            .rename(Blocks.NETHER_BRICKS, "nether_brick")
            .rename(Blocks.NETHER_QUARTZ_ORE, "quartz_ore")
            .rename(Blocks.SLIME_BLOCK, "slime")
            .rename(Blocks.PRISMARINE_BRICK_STAIRS, "prismarine_bricks_stairs")
            .rename(Blocks.END_STONE_BRICKS, "end_bricks")
            .rename(Blocks.END_STONE_BRICK_STAIRS, "end_brick_stairs")
            .rename(Blocks.BEETROOTS, "beetroot")
            .rename(Blocks.MAGMA_BLOCK, "magma")
            .rename(Blocks.RED_NETHER_BRICKS, "red_nether_brick")
            .rename(Blocks.SHULKER_BOX, "undyed_shulker_box")
            .rename(Blocks.KELP_PLANT, "kelp")
            .rename(Blocks.FROGSPAWN, "frog_spawn")
            .rename(Blocks.STONECUTTER, "stonecutter_block")
            .rename(Blocks.WEEPING_VINES_PLANT, "weeping_vines")
            .rename(Blocks.TWISTING_VINES_PLANT, "twisting_vines")

            // oak -> wooden / no prefix
            .rename(Blocks.OAK_SIGN, "standing_sign")
            .rename(Blocks.OAK_WALL_SIGN, "wall_sign")
            .rename(Blocks.OAK_TRAPDOOR, "trapdoor")
            .rename(Blocks.OAK_FENCE_GATE, "fence_gate")
            .rename(Blocks.OAK_DOOR, "wooden_door")
            .rename(Blocks.OAK_PRESSURE_PLATE, "wooden_pressure_plate")
            .rename(Blocks.OAK_BUTTON, "wooden_button")

            // State-dependent overrides
            .rename(Blocks.STONE_SLAB, state -> {
                if (state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.DOUBLE) {
                    return "normal_stone_double_slab";
                }
                return "normal_stone_slab";
            })
            .rename(Blocks.DEEPSLATE_REDSTONE_ORE, state -> {
                if (state.getValue(BlockStateProperties.LIT)) {
                    return "lit_deepslate_redstone_ore";
                }
                return "deepslate_redstone_ore";
            })
            .rename(Blocks.REDSTONE_ORE, state -> {
                if (state.getValue(BlockStateProperties.LIT)) {
                    return "lit_redstone_ore";
                }
                return "redstone_ore";
            })
            .rename(Blocks.REDSTONE_TORCH, state -> {
                if (state.getValue(BlockStateProperties.LIT)) {
                    return "redstone_torch";
                }
                return "unlit_redstone_torch";
            })
            .rename(Blocks.REDSTONE_LAMP, state -> {
                if (state.getValue(BlockStateProperties.LIT)) {
                    return "lit_redstone_lamp";
                }
                return "redstone_lamp";
            })
            .rename(Blocks.REDSTONE_WALL_TORCH, state -> {
                if (state.getValue(BlockStateProperties.LIT)) {
                    return "redstone_torch";
                }
                return "unlit_redstone_torch";
            })
            .rename(Blocks.PISTON_HEAD, state -> {
                if (state.getValue(BlockStateProperties.PISTON_TYPE) == PistonType.STICKY) {
                    return "sticky_piston_arm_collision";
                } else {
                    return "piston_arm_collision";
                }
            })
            .rename(Blocks.REPEATER, state -> {
                if (state.getValue(BlockStateProperties.POWERED)) {
                    return "powered_repeater";
                } else {
                    return "unpowered_repeater";
                }
            })
            .rename(Blocks.COMPARATOR, state -> {
                if (state.getValue(BlockStateProperties.POWERED)) {
                    return "powered_comparator";
                } else {
                    return "unpowered_comparator";
                }
            })
            .rename(Blocks.DAYLIGHT_DETECTOR, state -> {
                if (state.getValue(BlockStateProperties.INVERTED)) {
                    return "daylight_detector_inverted";
                } else {
                    return "daylight_detector";
                }
            })
            .rename(Blocks.LIGHT, state -> {
                int lightValue = state.getValue(LightBlock.LEVEL);
                return "light_block_" + lightValue;
            })
            .rename(Blocks.CAVE_VINES_PLANT, state -> {
                if (state.getValue(CaveVinesPlantBlock.BERRIES)) {
                    return "cave_vines_body_with_berries";
                } else {
                    return "cave_vines";
                }
            })
            .rename(Blocks.CAVE_VINES, state -> {
                if (state.getValue(CaveVinesPlantBlock.BERRIES)) {
                    return "cave_vines_head_with_berries";
                } else {
                    return "cave_vines";
                }
            })
            .rename(Blocks.WATER, state -> {
                if (state.getValue(LiquidBlock.LEVEL) == 0) {
                    return "water";
                } else {
                    return "flowing_water";
                }
            })
            .rename(Blocks.LAVA, state -> {
                if (state.getValue(LiquidBlock.LEVEL) == 0) {
                    return "lava";
                } else {
                    return "flowing_lava";
                }
            })
            .rename(Blocks.FURNACE, state -> {
                if (state.getValue(BlockStateProperties.LIT)) {
                    return "lit_furnace";
                } else {
                    return "furnace";
                }
            })
            .rename(Blocks.SMOKER, state -> {
                if (state.getValue(BlockStateProperties.LIT)) {
                    return "lit_smoker";
                } else {
                    return "smoker";
                }
            })
            .rename(Blocks.BLAST_FURNACE, state -> {
                if (state.getValue(BlockStateProperties.LIT)) {
                    return "lit_blast_furnace";
                } else {
                    return "blast_furnace";
                }
            }));
    
    private BlockNames() {}
    
    private static String mapName(BlockState state) {
        Block block = state.getBlock();
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
}
