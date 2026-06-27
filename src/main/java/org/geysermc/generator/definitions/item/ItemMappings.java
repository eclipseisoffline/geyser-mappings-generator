package org.geysermc.generator.definitions.item;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.geysermc.generator.mappings.FileType;
import org.geysermc.generator.mappings.MappingAccess;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class ItemMappings {
    private final Map<Holder<Item>, ItemEntry> itemMappings;
    private final List<RuntimeItemState> runtimeItemStates;
    private final List<Identifier> validBedrockItems;

    private ItemMappings(Map<Holder<Item>, ItemEntry> itemMappings, List<RuntimeItemState> runtimeItemStates) {
        this.itemMappings = itemMappings;
        this.runtimeItemStates = runtimeItemStates;
        this.validBedrockItems = runtimeItemStates.stream().map(RuntimeItemState::name).toList();
    }

    public static CompletableFuture<ItemMappings> open(MappingAccess access) {
        return access.readFile(FileType.ITEM_MAPPINGS).thenCombine(access.readFile(FileType.RUNTIME_ITEM_STATES), ItemMappings::new);
    }

    public Map<Holder<Item>, ItemEntry> itemMappings() {
        return itemMappings;
    }

    public List<RuntimeItemState> runtimeItemStates() {
        return runtimeItemStates;
    }

    public List<Identifier> validBedrockItems() {
        return validBedrockItems;
    }

    public void map(Item item, ItemEntry entry) {
        itemMappings.put(BuiltInRegistries.ITEM.wrapAsHolder(item), entry);
    }

    public ItemEntry computeIfAbsent(Item item, Supplier<ItemEntry> supplier) {
        return itemMappings.computeIfAbsent(BuiltInRegistries.ITEM.wrapAsHolder(item), _ -> supplier.get());
    }
}
