package org.geysermc.mappings.definitions.block;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.geysermc.mappings.FileType;
import org.geysermc.mappings.MappingsAccess;
import org.geysermc.mappings.util.MappingsCodecs;
import org.geysermc.mappings.util.MappingsUtil;
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
    public static final Codec<Map<BlockState, BlockEntry>> DEBUG_CODEC = Codec.unboundedMap(MappingsCodecs.BLOCK_STATE_STRING_CODEC, BlockEntry.CODEC);
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<BlockState, BlockEntry> mappings = new Object2ObjectLinkedOpenHashMap<>();
    private final BlockPalette blockPalette;

    private BlockMappings(Map<BlockState, BlockEntry> mappings, BlockPalette blockPalette) {
        this.blockPalette = blockPalette;
        this.mappings.putAll(mappings);
    }

    public static CompletableFuture<BlockMappings> open(MappingsAccess access) {
        // Not reading existing mappings as they're not used for anything (unlike e.g. item mappings)
        return access.readFile(FileType.BLOCK_PALETTE).thenApply(palette -> new BlockMappings(Map.of(), palette));
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
                LOGGER.error("Unknown bedrock block state: {} for Java state {}", mapped.getName(state.getBlock()), MappingsUtil.blockStateToString(state));
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
                .collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond));
    }

    private static List<BlockEntry> mappingsToList(Map<BlockState, BlockEntry> mappings) {
        return mappings.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> Block.BLOCK_STATE_REGISTRY.getId(entry.getKey())))
                .map(Map.Entry::getValue)
                .toList();
    }
}
