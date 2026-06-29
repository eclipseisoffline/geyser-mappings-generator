package org.geysermc.generator.definitions.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record BlockPalette(Map<Identifier, List<BlockPaletteItem>> blocks) {
    public static final Codec<BlockPalette> CODEC = BlockPaletteItem.CODEC.listOf().fieldOf("blocks")
            .xmap(list -> list.stream().collect(Collectors.groupingBy(BlockPaletteItem::name)),
                    map -> map.values().stream().flatMap(Collection::stream).toList())
            .xmap(BlockPalette::new, BlockPalette::blocks)
            .codec();

    public boolean hasBlockWithState(Identifier block, CompoundTag state) {
        List<BlockPaletteItem> states = blocks.get(block);
        if (states == null || states.isEmpty()) {
            return false;
        }
        if (state.isEmpty() && states.stream().anyMatch(item -> item.states.isEmpty())) {
            return true;
        }
        return states.stream().anyMatch(item -> item.states.equals(state));
    }

    public record BlockPaletteItem(Identifier name, int blockId, int networkId, long nameHash, int version, CompoundTag states) {
        public static final Codec<BlockPaletteItem> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Identifier.CODEC.fieldOf("name").forGetter(BlockPaletteItem::name),
                        Codec.INT.fieldOf("block_id").forGetter(BlockPaletteItem::blockId),
                        Codec.INT.fieldOf("network_id").forGetter(BlockPaletteItem::networkId),
                        Codec.LONG.fieldOf("name_hash").forGetter(BlockPaletteItem::nameHash),
                        Codec.INT.fieldOf("version").forGetter(BlockPaletteItem::version),
                        CompoundTag.CODEC.fieldOf("states").forGetter(BlockPaletteItem::states)
                ).apply(instance, BlockPaletteItem::new)
        );
    }
}
