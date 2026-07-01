package org.geysermc.mappings.generator;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.geysermc.mappings.definitions.component.ItemDataComponents;
import org.geysermc.mappings.FileType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class DataComponentGenerator extends MappingsGenerator<List<ItemDataComponents>> {
    private final CompletableFuture<RegistryAccess> registries;

    public DataComponentGenerator(PackOutput output, CompletableFuture<RegistryAccess> registries) {
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

    private static <T> void writeComponent(RegistryFriendlyByteBuf buf, TypedDataComponent<T> typed) {
        typed.type().streamCodec().encode(buf, typed.value());
    }
}
