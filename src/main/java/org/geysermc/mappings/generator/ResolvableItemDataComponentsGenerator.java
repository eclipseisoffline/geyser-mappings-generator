package org.geysermc.mappings.generator;

import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import org.geysermc.mappings.FileType;
import org.geysermc.mappings.definitions.component.TypedResolvableDataComponent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ResolvableItemDataComponentsGenerator extends MappingsGenerator<List<List<TypedResolvableDataComponent>>> {
    private final CompletableFuture<RegistryAccess> registries;

    public ResolvableItemDataComponentsGenerator(PackOutput output, CompletableFuture<RegistryAccess> registries) {
        super(output, FileType.RESOLVABLE_ITEM_DATA_COMPONENTS);
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return registries.thenCompose(registries -> saveFile(cache, registries, TypedResolvableDataComponent.collectAll()));
    }

    @Override
    public String getName() {
        return "Resolvable Item Data Components Generator";
    }
}
