package org.geysermc.mappings.mixin.accessor;

import net.minecraft.world.level.material.MapColor;
import org.geysermc.mappings.generator.javaclass.MapColorsGenerator;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/// Used in {@link MapColorsGenerator}
@Mixin(MapColor.class)
public interface MapColorAccessor {

    @Accessor("MATERIAL_COLORS")
    static @Nullable MapColor[] getMaterialColors() {
        throw new AssertionError();
    }
}
