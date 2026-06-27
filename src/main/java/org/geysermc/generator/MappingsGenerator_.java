package org.geysermc.generator;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.geysermc.generator.generator.BiomeMappingsGenerator;
import org.geysermc.generator.generator.CollisionsMappingsGenerator;
import org.geysermc.generator.generator.DataComponentGenerator;
import org.geysermc.generator.generator.ItemMappingsGenerator;
import org.geysermc.generator.generator.MapColorsGenerator;
import org.geysermc.generator.generator.ParticleMappingsGenerator;
import org.geysermc.generator.mixin.PackGeneratorAccessor;
import org.geysermc.generator.registries.RegistryAccessUtil;
import org.geysermc.generator.resources.BedrockSamples;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MappingsGenerator_ implements DataGeneratorEntrypoint {
    public static final String MOD_ID = "mappings-generator";

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        CompletableFuture<BedrockSamples> bedrockSamples = BedrockSamples.load(((PackGeneratorAccessor) (Object) pack).getOutput().getOutputFolder());
        // Have to use this instead of the registries provided by Fabric because these have tags
        CompletableFuture<RegistryAccess> registryAccess = RegistryAccessUtil.create();

        addProviderWithRegistries(pack, registryAccess, DataComponentGenerator::new);
        addProvider(pack, ItemMappingsGenerator::new);
        addProviderWithRegistries(pack, registryAccess, BiomeMappingsGenerator::new);
        addProvider(pack, MapColorsGenerator::new);
        addProviderWithSamples(pack, bedrockSamples, ParticleMappingsGenerator::new);
        addProvider(pack, CollisionsMappingsGenerator::new);
    }

    private static void addProvider(FabricDataGenerator.Pack pack, Function<PackOutput, DataProvider> factory) {
        // Java stuff
        pack.addProvider((FabricDataGenerator.Pack.Factory<? extends DataProvider>) factory::apply);
    }

    private static void addProviderWithRegistries(FabricDataGenerator.Pack pack, CompletableFuture<RegistryAccess> registryAccess,
                                                  BiFunction<PackOutput, CompletableFuture<RegistryAccess>, DataProvider> factory) {
        // You gotta love it
        pack.addProvider((FabricDataGenerator.Pack.Factory<? extends DataProvider>) output -> factory.apply(output, registryAccess));
    }

    private static void addProviderWithSamples(FabricDataGenerator.Pack pack, CompletableFuture<BedrockSamples> samples,
                                               BiFunction<PackOutput, CompletableFuture<BedrockSamples>, DataProvider> factory) {
        // Amazin'
        pack.addProvider((FabricDataGenerator.Pack.Factory<? extends DataProvider>) output -> factory.apply(output, samples));
    }
}
