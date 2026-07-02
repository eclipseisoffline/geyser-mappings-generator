package org.geysermc.mappings;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.geysermc.mappings.generator.BiomeMappingsGenerator;
import org.geysermc.mappings.generator.BlockMappingsGenerator;
import org.geysermc.mappings.generator.CollisionsMappingsGenerator;
import org.geysermc.mappings.generator.DataComponentGenerator;
import org.geysermc.mappings.generator.InteractionsGenerator;
import org.geysermc.mappings.generator.ItemComponentsGenerator;
import org.geysermc.mappings.generator.ItemMappingsGenerator;
import org.geysermc.mappings.generator.SoundMappingsGenerator;
import org.geysermc.mappings.generator.javaclass.BlockStatePropertiesGenerator;
import org.geysermc.mappings.generator.javaclass.BlocksGenerator;
import org.geysermc.mappings.generator.javaclass.GameRulesGenerator;
import org.geysermc.mappings.generator.javaclass.ItemsGenerator;
import org.geysermc.mappings.generator.javaclass.MapColorsGenerator;
import org.geysermc.mappings.generator.ParticleMappingsGenerator;
import org.geysermc.mappings.generator.UtilMappingsGenerator;
import org.geysermc.mappings.generator.javaclass.TagListGenerator;
import org.geysermc.mappings.generator.mcpl.BuiltinSoundGenerator;
import org.geysermc.mappings.generator.mcpl.ClientboundBlockEventPacketGenerator;
import org.geysermc.mappings.generator.mcpl.CustomStatisticGenerator;
import org.geysermc.mappings.generator.mcpl.LevelEventTypeGenerator;
import org.geysermc.mappings.generator.mcpl.NetworkCodecGenerator;
import org.geysermc.mappings.generator.mcpl.NetworkTagsGenerator;
import org.geysermc.mappings.mixin.PackGeneratorAccessor;
import org.geysermc.mappings.util.RegistryAccessUtil;
import org.geysermc.mappings.resources.BedrockSamples;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MappingsGenerators implements DataGeneratorEntrypoint {
    public static final String MOD_ID = "mappings-generator";

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        CompletableFuture<BedrockSamples> bedrockSamples = BedrockSamples.load(((PackGeneratorAccessor) (Object) pack).getOutput().getOutputFolder());
        // Have to use this instead of the registries provided by Fabric because these have tags
        CompletableFuture<RegistryAccess> registryAccess = RegistryAccessUtil.create();

        addProviderWithRegistries(pack, registryAccess, BlocksGenerator::new);
        addProvider(pack, BlockStatePropertiesGenerator::new);
        addProvider(pack, GameRulesGenerator::new);
        addProvider(pack, MapColorsGenerator::new);
        addProviderWithRegistries(pack, registryAccess, ItemsGenerator::new);

        TagListGenerator.addProviders(factory -> addProvider(pack, factory));

        addProviderWithRegistries(pack, registryAccess, DataComponentGenerator::new);
        addProvider(pack, ItemMappingsGenerator::new);
        addProviderWithRegistries(pack, registryAccess, BiomeMappingsGenerator::new);
        addProviderWithSamples(pack, bedrockSamples, ParticleMappingsGenerator::new);
        addProvider(pack, CollisionsMappingsGenerator::new);
        addProvider(pack, UtilMappingsGenerator::new);
        addProvider(pack, BlockMappingsGenerator::new);
        addProvider(pack, ItemComponentsGenerator::new);
        addProviderWithSamples(pack, bedrockSamples, SoundMappingsGenerator::new);
        addProvider(pack, InteractionsGenerator::new);

        addProvider(pack, BuiltinSoundGenerator::new);
        addProvider(pack, ClientboundBlockEventPacketGenerator::new);
        addProvider(pack, CustomStatisticGenerator::new);
        addProvider(pack, LevelEventTypeGenerator::new);
        addProviderWithRegistries(pack, registryAccess, NetworkCodecGenerator::new);
        addProviderWithRegistries(pack, registryAccess, NetworkTagsGenerator::new);
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
