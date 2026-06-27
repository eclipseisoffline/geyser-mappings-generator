package org.geysermc.generator.definitions.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record RuntimeItemState(Identifier name, int id, int version, boolean componentBased) {
    public static final Codec<RuntimeItemState> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("name").forGetter(RuntimeItemState::name),
                    Codec.INT.fieldOf("id").forGetter(RuntimeItemState::id),
                    Codec.INT.fieldOf("version").forGetter(RuntimeItemState::version),
                    Codec.BOOL.fieldOf("componentBased").forGetter(RuntimeItemState::componentBased)
            ).apply(instance, RuntimeItemState::new)
    );
}
