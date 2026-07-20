package org.geysermc.mappings.definitions.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.geysermc.mappings.mixin.accessor.BlockEntityTypesAccessor;
import org.geysermc.mappings.mixin.accessor.EntityTypesAccessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public record UtilMappings(List<Block> gameMasterBlocks, List<BlockEntityType<?>> dangerousBlockEntities,
                           List<EntityType<?>> dangerousEntities, List<Holder<Item>> thoseSpecialLilFlowersThatMakeYouFeelFunnyWhenYouEatThem) {
    public static final Codec<UtilMappings> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BuiltInRegistries.BLOCK.byNameCodec().listOf().fieldOf("game_master_blocks").forGetter(UtilMappings::gameMasterBlocks),
                    BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec().listOf().fieldOf("dangerous_block_entities").forGetter(UtilMappings::dangerousBlockEntities),
                    BuiltInRegistries.ENTITY_TYPE.byNameCodec().listOf().fieldOf("dangerous_entities").forGetter(UtilMappings::dangerousEntities),
                    Item.CODEC.listOf().fieldOf("suspicious_effect_holders").forGetter(UtilMappings::thoseSpecialLilFlowersThatMakeYouFeelFunnyWhenYouEatThem)
            ).apply(instance, UtilMappings::new)
    );

    public static UtilMappings create() {
        List<Block> gameMasterBlocks = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block instanceof GameMasterBlock) {
                gameMasterBlocks.add(block);
            }
        }

        List<Holder<Item>> thoseSpecialLilFlowersThatMakeYouFeelFunnyWhenYouEatThem = BuiltInRegistries.ITEM.stream()
                .filter(item -> SuspiciousEffectHolder.tryGet(item) != null)
                .map(BuiltInRegistries.ITEM::wrapAsHolder)
                .toList();

        return new UtilMappings(Collections.unmodifiableList(gameMasterBlocks),
                BlockEntityTypesAccessor.getOpOnlyCustomData().stream().sorted(Comparator.comparing(BuiltInRegistries.BLOCK_ENTITY_TYPE::getKey)).toList(),
                EntityTypesAccessor.getOpOnlyCustomData().stream().sorted(Comparator.comparing(BuiltInRegistries.ENTITY_TYPE::getKey)).toList(),
                thoseSpecialLilFlowersThatMakeYouFeelFunnyWhenYouEatThem);
    }
}
