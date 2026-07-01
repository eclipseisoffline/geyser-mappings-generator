package org.geysermc.generator.definitions.sound;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record SoundMapping(Optional<String> identifier, Optional<String> playSoundMapping,
                           Optional<String> bedrockMapping, int extraData,
                           double pitchAdjust, boolean levelEvent) {
    public static final Codec<SoundMapping> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("identifier").forGetter(SoundMapping::identifier),
                    Codec.STRING.optionalFieldOf("playsound_mapping").forGetter(SoundMapping::playSoundMapping),
                    Codec.STRING.optionalFieldOf("bedrock_mapping").forGetter(SoundMapping::bedrockMapping),
                    Codec.INT.optionalFieldOf("extra_data", -1).forGetter(SoundMapping::extraData),
                    Codec.DOUBLE.optionalFieldOf("pitch_adjust", 1.0).forGetter(SoundMapping::pitchAdjust),
                    Codec.BOOL.optionalFieldOf("level_event", false).forGetter(SoundMapping::levelEvent)
            ).apply(instance, SoundMapping::new)
    );
    public static final SoundMapping EMPTY = new SoundMapping(Optional.empty(), Optional.empty(), Optional.empty(),
            -1, 1.0, false);
}
