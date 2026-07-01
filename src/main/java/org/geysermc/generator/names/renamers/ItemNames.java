package org.geysermc.generator.names.renamers;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.geysermc.generator.names.InstanceRenamer;

public final class ItemNames {
    public static final InstanceRenamer<Item, Identifier, Identifier> INSTANCE = InstanceRenamer.of(ItemNames::mapItemIdentifier, builder -> builder
            .mapResult(Identifier::withDefaultNamespace)
            // Conflicts
            .rename(Items.MAP, "empty_map") // Conflicts with filled map
            .rename(Items.MELON, "melon_block") // Conflicts with melon slice
            .rename(Items.NETHER_BRICK, "netherbrick") // This is the item; the block conflicts
            .rename(Items.NETHER_BRICKS, "nether_brick")
            .rename(Items.SNOW, "snow_layer") // Conflicts with snow block
            .rename(Items.SNOW_BLOCK, "snow")
            .rename(Items.STONE_STAIRS, "normal_stone_stairs") // Conflicts with cobblestone stairs
            .rename(Items.COBBLESTONE_STAIRS, "stone_stairs")
            .rename(Items.STONECUTTER, "stonecutter_block") // Conflicts with, surprisingly, the OLD MCPE stonecutter
            // Changed names
            .rename(Items.FROGSPAWN, "frog_spawn")
            .rename(Items.GLOW_ITEM_FRAME, "glow_frame")
            .rename(Items.ITEM_FRAME, "frame")
            .rename(Items.OAK_DOOR, "wooden_door")
            .rename(Items.SHULKER_BOX, "undyed_shulker_box")
            .rename(Items.SMALL_DRIPLEAF, "small_dripleaf_block")
            .rename(Items.WAXED_COPPER_BLOCK, "waxed_copper")
            .rename(Items.ZOMBIFIED_PIGLIN_SPAWN_EGG, "zombie_pigman_spawn_egg")
            // don't exist on bedrock edition (yet)
            .rename(Items.TEST_BLOCK, "unknown")
            .rename(Items.TEST_INSTANCE_BLOCK, "unknown")
            // Replaced by us
            .rename(Items.KNOWLEDGE_BOOK, "book")
            .rename(Items.TIPPED_ARROW, "arrow")
            .rename(Items.DEBUG_STICK, "stick")
            .rename(Items.FURNACE_MINECART, "hopper_minecart"));

    private ItemNames() {}

    private static Identifier mapItemIdentifier(Identifier baseBedrockIdentifier) {
        String bedrock = baseBedrockIdentifier.getPath();

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

        return Identifier.fromNamespaceAndPath(baseBedrockIdentifier.getNamespace(), bedrock);
    }
}
