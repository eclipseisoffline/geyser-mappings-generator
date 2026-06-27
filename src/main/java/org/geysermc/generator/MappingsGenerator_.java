package org.geysermc.generator;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.geysermc.generator.registries.RegistryAccessUtil;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class MappingsGenerator_ implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        // Have to use this instead of the registries provided by Fabric because these have tags
        CompletableFuture<RegistryAccess> registryAccess = RegistryAccessUtil.create();

        addProvider(pack, registryAccess, DataComponentGenerator::new);
    }

    private static void addProvider(FabricDataGenerator.Pack pack, CompletableFuture<RegistryAccess> registryAccess,
                                    BiFunction<PackOutput, CompletableFuture<RegistryAccess>, DataProvider> factory) {
        // Java stuff
        pack.addProvider((FabricDataGenerator.Pack.Factory<? extends DataProvider>) output -> factory.apply(output, registryAccess));
    }
}
