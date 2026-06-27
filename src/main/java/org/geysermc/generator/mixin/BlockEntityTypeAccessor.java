package org.geysermc.generator.mixin;

import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(BlockEntityType.class)
public interface BlockEntityTypeAccessor {

    @Accessor("OP_ONLY_CUSTOM_DATA")
    static Set<BlockEntityType<?>> getOpOnlyCustomData() {
        throw new AssertionError();
    }
}
