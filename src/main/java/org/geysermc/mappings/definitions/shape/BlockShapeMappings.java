package org.geysermc.mappings.definitions.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.List;

public record BlockShapeMappings(IntList indices, List<List<List<Double>>> shapes) {
    public static final Codec<BlockShapeMappings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT_STREAM.fieldOf("indices").xmap(stream -> (IntList) new IntArrayList(stream.toArray()), IntCollection::intStream).forGetter(BlockShapeMappings::indices),
                    Codec.list(Codec.list(Codec.list(Codec.DOUBLE))).fieldOf("shapes").forGetter(BlockShapeMappings::shapes)
            ).apply(instance, BlockShapeMappings::new)
    );
}
