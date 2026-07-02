package org.geysermc.mappings.mixin;

import net.minecraft.world.level.material.MapColor;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MapColor.class)
public interface MapColorAccessor {

    @Accessor("MATERIAL_COLORS")
    static @Nullable MapColor[] getMaterialColors() {
        throw new AssertionError();
    }
}
