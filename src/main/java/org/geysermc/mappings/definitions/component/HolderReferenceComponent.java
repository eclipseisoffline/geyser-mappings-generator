package org.geysermc.mappings.definitions.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;

public record HolderReferenceComponent(Identifier registry, Identifier reference, boolean allowsDirectOverNetwork) implements ResolvableDataComponent {
    public static final MapCodec<HolderReferenceComponent> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("registry").forGetter(HolderReferenceComponent::registry),
                    Identifier.CODEC.fieldOf("reference").forGetter(HolderReferenceComponent::reference),
                    Codec.BOOL.fieldOf("allows_direct_over_network").forGetter(HolderReferenceComponent::allowsDirectOverNetwork)
            ).apply(instance, HolderReferenceComponent::new)
    );

    public HolderReferenceComponent(Holder.Reference<?> holder, boolean allowsDirectOverNetwork) {
        this(holder.key().registry(), holder.key().identifier(), allowsDirectOverNetwork);
    }

    @Override
    public Type type() {
        return Type.HOLDER;
    }
}
