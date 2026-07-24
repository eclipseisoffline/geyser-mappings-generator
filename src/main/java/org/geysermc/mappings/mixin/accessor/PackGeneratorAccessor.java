package org.geysermc.mappings.mixin.accessor;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.geysermc.mappings.MappingsGenerators;
import org.geysermc.mappings.resources.BedrockSamples;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/// Used in {@link MappingsGenerators} to get the output folder of the generator before the {@link DataProvider}s are initialised,
/// so we can start the {@link BedrockSamples} downloader early
@Mixin(DataGenerator.PackGenerator.class)
public interface PackGeneratorAccessor {

    @Accessor
    PackOutput getOutput();
}
