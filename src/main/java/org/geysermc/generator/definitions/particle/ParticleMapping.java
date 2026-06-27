package org.geysermc.generator.definitions.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import org.cloudburstmc.protocol.bedrock.data.LevelEvent;
import org.cloudburstmc.protocol.bedrock.data.ParticleType;

import java.util.Optional;

/// @param cloudburstEventType the {@link LevelEvent} or {@link ParticleType}
public record ParticleMapping(Optional<Identifier> bedrockId, Optional<String> cloudburstEventType) {
    public static final Codec<ParticleMapping> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.optionalFieldOf("bedrockId").forGetter(ParticleMapping::bedrockId),
                    Codec.STRING.optionalFieldOf("eventType").forGetter(ParticleMapping::cloudburstEventType)
            ).apply(instance, ParticleMapping::new)
    );
    public static final ParticleMapping EMPTY = new ParticleMapping(Optional.empty(), Optional.empty());
}
