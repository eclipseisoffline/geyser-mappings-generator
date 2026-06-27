package org.geysermc.generator.definitions.collision;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.List;

public record CollisionsMappings(IntList indices, List<List<List<Double>>> collisions) {
    public static final Codec<CollisionsMappings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT_STREAM.fieldOf("indices").xmap(stream -> (IntList) new IntArrayList(stream.toArray()), IntCollection::intStream).forGetter(CollisionsMappings::indices),
                    Codec.list(Codec.list(Codec.list(Codec.DOUBLE))).fieldOf("collisions").forGetter(CollisionsMappings::collisions)
            ).apply(instance, CollisionsMappings::new)
    );
}
