package org.geysermc.mappings.definitions.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record BedrockBiome(int id, Identifier name) {
    public static final Codec<BedrockBiome> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("id").forGetter(BedrockBiome::id),
                    Identifier.CODEC.fieldOf("name").forGetter(BedrockBiome::name)
            ).apply(instance, BedrockBiome::new)
    );
}
