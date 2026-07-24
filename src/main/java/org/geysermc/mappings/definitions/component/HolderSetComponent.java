package org.geysermc.mappings.definitions.component;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public record HolderSetComponent(Optional<Identifier> registry, List<Identifier> references) implements ResolvableDataComponent {
    public static final MapCodec<HolderSetComponent> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC.optionalFieldOf("registry").forGetter(HolderSetComponent::registry),
                    Identifier.CODEC.listOf().optionalFieldOf("references", List.of()).forGetter(HolderSetComponent::references)
            ).apply(instance, HolderSetComponent::new)
    );

    @Override
    public Type type() {
        return Type.HOLDER_SET;
    }
}
