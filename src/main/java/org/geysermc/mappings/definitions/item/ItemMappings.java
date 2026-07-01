package org.geysermc.mappings.definitions.item;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.geysermc.mappings.FileType;
import org.geysermc.mappings.MappingsAccess;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class ItemMappings {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<Item, ItemEntry> mappings;
    private final RuntimeItemStates runtimeItemStates;

    private ItemMappings(Map<Item, ItemEntry> mappings, RuntimeItemStates runtimeItemStates) {
        this.mappings = new Object2ObjectOpenHashMap<>(mappings);
        this.runtimeItemStates = runtimeItemStates;
    }

    public static CompletableFuture<ItemMappings> open(MappingsAccess access) {
        return access.readFile(FileType.ITEM_MAPPINGS).thenCombine(access.readFile(FileType.RUNTIME_ITEM_STATES), ItemMappings::new);
    }

    public Map<Item, ItemEntry> mappings() {
        return mappings;
    }

    public RuntimeItemStates runtimeItemStates() {
        return runtimeItemStates;
    }

    public void map(Item item, ItemEntry entry) {
        mappings.put(item, entry);
    }

    public void mapAllItems(BiFunction<Identifier, Item, ItemEntry> mapper) {
        boolean success = true;
        for (Item item : BuiltInRegistries.ITEM) {
            Identifier key = BuiltInRegistries.ITEM.getKey(item);
            ItemEntry entry = mapper.apply(key, item);
            if (runtimeItemStates.states().containsKey(entry.bedrockIdentifier())) {
                map(item, entry);
            } else {
                success = false;
                LOGGER.error("Java item {} was mapped to bedrock item {}, which does not exist in runtime_item_states!", key, entry.bedrockIdentifier());
            }
        }
        if (!success) {
            LOGGER.error("Errors occurred whilst mapping items, ITEM MAPPINGS ARE INCOMPLETE!");
        }
    }

    public void checkForDuplicates() {
        Map<ItemEntry, Item> duplications = new Object2ObjectOpenHashMap<>();
        for (Map.Entry<Item, ItemEntry> mapping : mappings.entrySet()) {
            Item duplicate = duplications.get(mapping.getValue());
            if (duplicate != null) {
                LOGGER.warn("Possible duplicate item mapping ({} and {}) in mappings: {}", mapping.getKey(), duplicate, mapping.getValue());
            } else {
                duplications.put(mapping.getValue(), mapping.getKey());
            }
        }
    }

    public ItemEntry computeIfAbsent(Item item, Supplier<ItemEntry> supplier) {
        return mappings.computeIfAbsent(item, _ -> supplier.get());
    }
}
