package org.geysermc.generator.definitions.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.geysermc.generator.mixin.BlockEntityTypeAccessor;
import org.geysermc.generator.mixin.EntityTypeAccessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public record UtilMappings(List<Block> gameMasterBlocks, List<BlockEntityType<?>> dangerousBlockEntities,
                           List<EntityType<?>> dangerousEntities) {
    public static final Codec<UtilMappings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.BLOCK.byNameCodec().listOf().fieldOf("game_master_blocks").forGetter(UtilMappings::gameMasterBlocks),
                    BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec().listOf().fieldOf("dangerous_block_entities").forGetter(UtilMappings::dangerousBlockEntities),
                    BuiltInRegistries.ENTITY_TYPE.byNameCodec().listOf().fieldOf("dangerous_entities").forGetter(UtilMappings::dangerousEntities)
            ).apply(instance, UtilMappings::new)
    );

    public static UtilMappings create() {
        List<Block> gameMasterBlocks = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block instanceof GameMasterBlock) {
                gameMasterBlocks.add(block);
            }
        }

        return new UtilMappings(Collections.unmodifiableList(gameMasterBlocks),
                BlockEntityTypeAccessor.getOpOnlyCustomData().stream().sorted(Comparator.comparing(BuiltInRegistries.BLOCK_ENTITY_TYPE::getKey)).toList(),
                EntityTypeAccessor.getOpOnlyCustomData().stream().sorted(Comparator.comparing(BuiltInRegistries.ENTITY_TYPE::getKey)).toList());
    }
}
