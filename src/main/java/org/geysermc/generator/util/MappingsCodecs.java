package org.geysermc.generator.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;

public final class MappingsCodecs {
    public static final Codec<Identifier> TRIMMED_IDENTIFIER_CODEC = Codec.of(Codec.STRING.comap(Identifier::toShortString), Identifier.CODEC);
    public static final Codec<SoundEvent> TRIMMED_SOUND_EVENT_CODEC = TRIMMED_IDENTIFIER_CODEC.flatXmap(identifier -> BuiltInRegistries.SOUND_EVENT.get(identifier)
                    .map(Holder::value)
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown sound event: " + identifier)),
            soundEvent -> BuiltInRegistries.SOUND_EVENT.getResourceKey(soundEvent)
                    .map(ResourceKey::identifier)
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unregistered sound event: " + soundEvent)));
    public static final Codec<BlockState> BLOCK_STATE_STRING_CODEC = Codec.STRING.comapFlatMap(MappingsUtil::blockStateFromString, MappingsUtil::blockStateToString);

    private MappingsCodecs() {}
}
