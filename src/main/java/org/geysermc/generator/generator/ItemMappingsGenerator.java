package org.geysermc.generator.generator;

import com.mojang.datafixers.util.Pair;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.geysermc.generator.definitions.item.ItemEntry;
import org.geysermc.generator.definitions.item.ItemMappings;
import org.geysermc.generator.mappings.FileType;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ItemMappingsGenerator extends MappingsGenerator<Map<Item, ItemEntry>> {
    // Fix some discrepancies - key is the Java item and value is the Bedrock item identifier
    private static final Map<Item, String> JAVA_TO_BEDROCK_OVERRIDES = Stream.of(
            // Conflicts
            Pair.of(Items.MAP, "empty_map"), // Conflicts with filled map
            Pair.of(Items.MELON, "melon_block"), // Conflicts with melon slice
            Pair.of(Items.NETHER_BRICK, "netherbrick"), // This is the item; the block conflicts
            Pair.of(Items.NETHER_BRICKS, "nether_brick"),
            Pair.of(Items.SNOW, "snow_layer"), // Conflicts with snow block
            Pair.of(Items.SNOW_BLOCK, "snow"),
            Pair.of(Items.STONE_STAIRS, "normal_stone_stairs"), // Conflicts with cobblestone stairs
            Pair.of(Items.COBBLESTONE_STAIRS, "stone_stairs"),
            Pair.of(Items.STONECUTTER, "stonecutter_block"), // Conflicts with, surprisingly, the OLD MCPE stonecutter

            // Changed names
            Pair.of(Items.FROGSPAWN, "frog_spawn"),
            Pair.of(Items.GLOW_ITEM_FRAME, "glow_frame"),
            Pair.of(Items.ITEM_FRAME, "frame"),
            Pair.of(Items.OAK_DOOR, "wooden_door"),
            Pair.of(Items.SHULKER_BOX, "undyed_shulker_box"),
            Pair.of(Items.SMALL_DRIPLEAF, "small_dripleaf_block"),
            Pair.of(Items.WAXED_COPPER_BLOCK, "waxed_copper"),
            Pair.of(Items.ZOMBIFIED_PIGLIN_SPAWN_EGG, "zombie_pigman_spawn_egg"),

            // don't exist on bedrock edition (yet)
            Pair.of(Items.TEST_BLOCK, "unknown"),
            Pair.of(Items.TEST_INSTANCE_BLOCK, "unknown"),

            Pair.of(Items.KNOWLEDGE_BOOK, "book"),
            Pair.of(Items.TIPPED_ARROW, "arrow"),
            Pair.of(Items.DEBUG_STICK, "stick"),
            Pair.of(Items.FURNACE_MINECART, "hopper_minecart")
    )
            .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

    public ItemMappingsGenerator(PackOutput output) {
        super(output, FileType.ITEM_MAPPINGS);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return ItemMappings.open(this).thenCompose(mappings -> {
            mappings.mapAllItems((key, item) -> getRemapItem(mappings, key, item, Block.byItem(item)));
            mappings.checkForDuplicates();
            return saveFile(cache, mappings.mappings());
        });
    }

    private ItemEntry getRemapItem(ItemMappings mappings, Identifier javaIdentifier, Item item, Block block) {
        // Ignore items that require experiments
        if (FeatureFlags.isExperimental(item.requiredFeatures())) {
            return ItemEntry.UNKNOWN;
        }

        ItemEntry base = mappings.computeIfAbsent(item, () -> new ItemEntry(javaIdentifier, 0));
        Identifier bedrockIdentifier = mapBedrockIdentifier(item, base.bedrockIdentifier());
        boolean isBlock = block != Blocks.AIR;
        int bedrockData = isBlock ? base.bedrockData() : 0;
        int firstStateId = -1;
        int lastStateId = -1;
        boolean entityPlacer = base.entityPlacer();

        if (isBlock) {
            for (BlockState state : Block.BLOCK_STATE_REGISTRY) {
                if (state.getBlock() == block) {
                    int stateId = Block.getId(state);
                    if (firstStateId == -1) {
                        firstStateId = stateId;
                    }
                    lastStateId = stateId;
                } else if (lastStateId != -1) {
                    break;
                }
            }
        }

        if (item instanceof SpawnEggItem || item instanceof MinecartItem || item instanceof FireworkRocketItem || item instanceof BoatItem) {
            entityPlacer = true;
        }

        return new ItemEntry(bedrockIdentifier, bedrockData, isBlock, firstStateId, lastStateId, entityPlacer);
    }

    private static Identifier mapBedrockIdentifier(Item item, Identifier bedrockIdentifier) {
        String bedrock = JAVA_TO_BEDROCK_OVERRIDES.getOrDefault(item, bedrockIdentifier.getPath());

        if (bedrock.endsWith("banner")) { // Don't include banner patterns
            bedrock = "banner";
        } else if (bedrock.endsWith("bed")) {
            bedrock = "bed";
        }

        if (bedrock.startsWith("stone_slab") || bedrock.startsWith("double_stone_slab")) {
            bedrock = bedrock.replace("stone_slab", "stone_block_slab");
        }
        if (bedrock.startsWith("double_stone_block_slab")) {
            bedrock = bedrock.replace("double_stone_block_slab", "stone_block_slab");
        }

        return Identifier.parse(bedrock);
    }

    @Override
    public String getName() {
        return "Item Mappings Generator";
    }
}
