package org.geysermc.generator.definitions.block;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.data.models.blockstates.PropertyValueList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.geysermc.generator.mappings.FileType;
import org.geysermc.generator.mappings.MappingAccess;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class BlockMappings {
    public static final Codec<Map<BlockState, BlockEntry>> CODEC = BlockEntry.CODEC.listOf().fieldOf("bedrock_mappings")
            .xmap(BlockMappings::listToMappings, BlockMappings::mappingsToList)
            .codec();
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<BlockState, BlockEntry> mappings = new Object2ObjectLinkedOpenHashMap<>();
    private final BlockPalette blockPalette;

    private BlockMappings(Map<BlockState, BlockEntry> mappings, BlockPalette blockPalette) {
        this.blockPalette = blockPalette;
        this.mappings.putAll(mappings);
    }

    public static CompletableFuture<BlockMappings> open(MappingAccess access) {
        return access.readNbtFile(FileType.BLOCK_PALETTE).thenApply(palette -> new BlockMappings(Map.of(), palette));
    }

    public Map<BlockState, BlockEntry> mappings() {
        return mappings;
    }

    public BlockPalette blockPalette() {
        return blockPalette;
    }

    public void map(BlockState state, BlockEntry entry) {
        mappings.put(state, entry);
    }

    public void mapAllStates(Function<BlockState, BlockEntry> mapper) {
        boolean success = true;
        for (BlockState state : Block.BLOCK_STATE_REGISTRY) {
            BlockEntry mapped = mapper.apply(state);
            if (blockPalette.hasBlockWithState(mapped.getName(state.getBlock()), mapped.state())) {
                map(state, mapped);
            } else {
                success = false;
                LOGGER.error("Unknown bedrock block state: {} for Java state {}", mapped.getName(state.getBlock()), blockStateToString(state));
                if (!mapped.state().isEmpty()) {
                    LOGGER.error("Bedrock block state NBT: {}", mapped.state());
                }
            }
        }

        if (!success) {
            LOGGER.error("Errors occurred whilst mapping block states, BLOCK MAPPINGS ARE INCOMPLETE!");
        }
    }

    private static Map<BlockState, BlockEntry> listToMappings(List<BlockEntry> entries) {
        return IntStream.range(0, entries.size())
                .mapToObj(i -> Pair.of(Block.BLOCK_STATE_REGISTRY.byIdOrThrow(i), entries.get(i)))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    private static List<BlockEntry> mappingsToList(Map<BlockState, BlockEntry> mappings) {
        return mappings.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> Block.BLOCK_STATE_REGISTRY.getId(entry.getKey())))
                .map(Map.Entry::getValue)
                .toList();
    }

    public static String blockStateToString(BlockState state) {
        Identifier identifier = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (state.isSingletonState()) {
            return identifier.toString();
        }
        return identifier + "[" + blockStatePropertiesToString(state) + "]";
    }

    // thank you rainbow
    private static String blockStatePropertiesToString(BlockState state) {
        return PropertyValueList.of(state.getProperties().stream().map(property -> property.value(state)).toArray(Property.Value[]::new)).getKey();
    }
}
