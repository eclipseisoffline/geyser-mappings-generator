package org.geysermc.mappings.definitions.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.StringRepresentable;

public interface ResolvableDataComponent {

    MapCodec<ResolvableDataComponent> MAP_CODEC = Type.CODEC.dispatchMap(ResolvableDataComponent::type, type -> type.codec);

    Type type();

    enum Type implements StringRepresentable {
        HOLDER("holder", HolderReferenceComponent.MAP_CODEC),
        HOLDER_SET("holder_set", HolderSetComponent.MAP_CODEC);

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        private final String name;
        private final MapCodec<? extends ResolvableDataComponent> codec;

        Type(String name, MapCodec<? extends ResolvableDataComponent> codec) {
            this.name = name;
            this.codec = codec;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
