package org.geysermc.generator.generator;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.geysermc.generator.definitions.block.BlockEntry;
import org.geysermc.generator.definitions.block.BlockMappings;
import org.geysermc.generator.definitions.block.BlockNames;
import org.geysermc.generator.definitions.block.state.BlockMapper;
import org.geysermc.generator.definitions.block.state.BlockMappers;
import org.geysermc.generator.mappings.FileType;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class BlockMappingsGenerator extends MappingsGenerator<Map<BlockState, BlockEntry>> {

    public BlockMappingsGenerator(PackOutput output) {
        super(output, FileType.BLOCK_MAPPINGS);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        BlockMappers.registerMappers();
        return BlockMappings.open(this).thenCompose(mappings -> {
            mappings.mapAllStates(javaState -> {
                String bedrockName = BlockNames.getName(javaState);
                CompoundTag bedrockState = new CompoundTag();

                for (BlockMapper mapper : BlockMapper.ALL_MAPPERS) {
                    mapper.apply(javaState, bedrockState);
                }
                return BlockEntry.of(javaState.getBlock(), bedrockName, bedrockState);
            });
            return CompletableFuture.allOf(saveFile(cache, mappings.mappings()), saveFile(cache, FileType.BLOCK_MAPPINGS_DEBUG, mappings.mappings()));
        });
    }

    @Override
    public String getName() {
        return "Block Mappings Generator";
    }
}
