package org.geysermc.mappings.generator.shape;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import org.geysermc.mappings.FileType;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.StreamSupport;

public final class CollisionShapeMappingsGenerator extends AbstractShapeMappingsGenerator {
    private static final Logger LOGGER = LogUtils.getLogger();

    public CollisionShapeMappingsGenerator(PackOutput output) {
        super(output, FileType.COLLISION_MAPPINGS);
    }

    @Override
    protected List<List<AABB>> collectShapes() {
        return StreamSupport.stream(Block.BLOCK_STATE_REGISTRY.spliterator(), false)
                .map(state -> {
                    try {
                        return state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).toAabbs();
                    } catch (Exception exception) {
                        LOGGER.error("Failed to get collision shapes for {}", state, exception);
                        return List.<AABB>of();
                    }
                })
                .toList();
    }

    @Override
    public String getName() {
        return "Collision Shape Mappings Generator";
    }
}
