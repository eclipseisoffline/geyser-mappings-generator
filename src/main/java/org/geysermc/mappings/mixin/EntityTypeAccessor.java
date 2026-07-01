package org.geysermc.mappings.mixin;

import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(EntityType.class)
public interface EntityTypeAccessor {

    @Accessor("OP_ONLY_CUSTOM_DATA")
    static Set<EntityType<?>> getOpOnlyCustomData() {
        throw new AssertionError();
    }
}
