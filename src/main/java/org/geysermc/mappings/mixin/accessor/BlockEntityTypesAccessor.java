package org.geysermc.mappings.mixin.accessor;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import org.geysermc.mappings.definitions.util.UtilMappings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

/// Used in {@link UtilMappings}
@Mixin(BlockEntityTypes.class)
public interface BlockEntityTypesAccessor {

    @Accessor("OP_ONLY_CUSTOM_DATA")
    static Set<BlockEntityType<?>> getOpOnlyCustomData() {
        throw new AssertionError();
    }
}
