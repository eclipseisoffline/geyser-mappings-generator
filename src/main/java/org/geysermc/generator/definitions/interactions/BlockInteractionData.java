package org.geysermc.generator.definitions.interactions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.world.level.block.state.BlockState;
import org.geysermc.generator.util.MappingsCodecs;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record BlockInteractionData(List<BlockState> alwaysConsumes, List<BlockState> requiresMayBuild, int dataVersion) {
    public static final Codec<BlockInteractionData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    MappingsCodecs.BLOCK_STATE_STRING_CODEC.listOf().fieldOf("always_consumes").forGetter(BlockInteractionData::alwaysConsumes),
                    MappingsCodecs.BLOCK_STATE_STRING_CODEC.listOf().fieldOf("requires_may_build").forGetter(BlockInteractionData::requiresMayBuild),
                    Codec.INT.fieldOf("data_version").forGetter(BlockInteractionData::dataVersion)
            ).apply(instance, BlockInteractionData::new)
    );

    public boolean isAccurate() {
        return dataVersion == SharedConstants.getCurrentVersion().dataVersion().version();
    }

    public static BlockInteractionData of(Stream<BlockStateRequirement> stream) {
        Map<Requirement, List<BlockStateRequirement>> requirements = stream.collect(Collectors.groupingBy(BlockStateRequirement::requirement));
        List<BlockState> alwaysConsumes = requirements.getOrDefault(Requirement.ALWAYS, List.of()).stream().map(BlockStateRequirement::state).toList();
        List<BlockState> requiresMayBuild = requirements.getOrDefault(Requirement.REQUIRES_MAY_BUILD, List.of()).stream().map(BlockStateRequirement::state).toList();
        return new BlockInteractionData(alwaysConsumes, requiresMayBuild, SharedConstants.getCurrentVersion().dataVersion().version());
    }

    public record BlockStateRequirement(BlockState state, Requirement requirement) {}

    public enum Requirement {
        ALWAYS,
        REQUIRES_MAY_BUILD,
        UNKNOWN
    }
}
