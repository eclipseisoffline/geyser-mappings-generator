package org.geysermc.generator.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;

public final class MappingsCodecs {
    // Unsafe parse
    public static final Codec<Identifier> TRIMMED_IDENTIFIER_CODEC = Codec.STRING.xmap(Identifier::parse, Identifier::toShortString);
    public static final Codec<SoundEvent> TRIMMED_SOUND_EVENT_CODEC = TRIMMED_IDENTIFIER_CODEC.flatXmap(identifier -> BuiltInRegistries.SOUND_EVENT.get(identifier)
                    .map(Holder::value)
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown sound event: " + identifier)),
            soundEvent -> BuiltInRegistries.SOUND_EVENT.getResourceKey(soundEvent)
                    .map(ResourceKey::identifier)
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unregistered sound event: " + soundEvent)));

    private MappingsCodecs() {}
}
