package org.geysermc.generator.definitions.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public record BlockEntry(Optional<String> bedrockIdentifier, CompoundTag state) {
    private static final CompoundTag EMPTY_TAG = new CompoundTag();
    public static final Codec<BlockEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("bedrock_identifier").forGetter(BlockEntry::bedrockIdentifier),
                    CompoundTag.CODEC.optionalFieldOf("state", EMPTY_TAG).forGetter(BlockEntry::state)
            ).apply(instance, BlockEntry::new)
    );

    public static BlockEntry of(Block block, String name) {
        return of(block, name, EMPTY_TAG);
    }

    public static BlockEntry of(Block block, String name, CompoundTag state) {
        String javaName = BuiltInRegistries.BLOCK.getKey(block).getPath();
        if (javaName.equals(name)) {
            return new BlockEntry(Optional.empty(), state);
        }
        return new BlockEntry(Optional.of(name), state);
    }

    public Identifier getName(Block javaBlock) {
        return bedrockIdentifier
                .map(Identifier::withDefaultNamespace)
                .orElseGet(() -> BuiltInRegistries.BLOCK.getKey(javaBlock));
    }
}
