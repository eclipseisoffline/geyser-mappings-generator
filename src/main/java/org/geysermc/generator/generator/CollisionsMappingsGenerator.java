package org.geysermc.generator.generator;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.geysermc.generator.definitions.collision.CollisionsMappings;
import org.geysermc.generator.mappings.FileType;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class CollisionsMappingsGenerator extends MappingsGenerator<CollisionsMappings> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public CollisionsMappingsGenerator(PackOutput output) {
        super(output, FileType.COLLISION_MAPPINGS);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<List<List<Double>>> collisionList = new ArrayList<>();

        IntList indices = new IntArrayList(Block.BLOCK_STATE_REGISTRY.size());
        for (BlockState state : Block.BLOCK_STATE_REGISTRY) {
            List<List<Double>> collisionBoxes = Lists.newArrayList();
            try {
                state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).toAabbs().forEach(item -> {
                    List<Double> coordinateList = Lists.newArrayList();
                    // Convert Box class to an array of coordinates
                    // They need to be converted from min/max coordinates to centres and sizes
                    coordinateList.add(item.minX + ((item.maxX - item.minX) / 2));
                    coordinateList.add(item.minY + ((item.maxY - item.minY) / 2));
                    coordinateList.add(item.minZ + ((item.maxZ - item.minZ) / 2));

                    coordinateList.add(item.maxX - item.minX);
                    coordinateList.add(item.maxY - item.minY);
                    coordinateList.add(item.maxZ - item.minZ);

                    collisionBoxes.add(coordinateList);
                });
            } catch (Exception exception) {
                LOGGER.warn("Failed to get collision for {}", state, exception);
            }
            if (!collisionList.contains(collisionBoxes)) {
                collisionList.add(collisionBoxes);
            }
            indices.add(collisionList.lastIndexOf(collisionBoxes));
        }

        return saveFile(cache, new CollisionsMappings(indices, collisionList));
    }

    @Override
    public String getName() {
        return "Collisions Mappings Generator";
    }
}
