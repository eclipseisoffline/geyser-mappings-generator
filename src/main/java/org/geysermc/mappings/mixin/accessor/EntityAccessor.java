package org.geysermc.mappings.mixin.accessor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.geysermc.mappings.generator.InteractionsGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/// Used in {@link InteractionsGenerator}
@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("position")
    void setPositionDirectly(Vec3 position);
}
