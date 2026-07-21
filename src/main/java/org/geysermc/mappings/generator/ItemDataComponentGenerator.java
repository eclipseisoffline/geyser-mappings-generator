package org.geysermc.mappings.generator;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import org.geysermc.mappings.definitions.component.ItemDataComponents;
import org.geysermc.mappings.FileType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ItemDataComponentGenerator extends MappingsGenerator<List<ItemDataComponents>> {
    private final CompletableFuture<RegistryAccess> registries;

    public ItemDataComponentGenerator(PackOutput output, CompletableFuture<RegistryAccess> registries) {
        super(output, FileType.ITEM_DATA_COMPONENTS);
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return registries.thenCompose(registries ->
                saveFile(cache, BuiltInRegistries.ITEM.stream().map(item -> ItemDataComponents.create(item, registries)).toList()));
    }

    @Override
    public String getName() {
        return "Default Data Component Generator";
    }
}
