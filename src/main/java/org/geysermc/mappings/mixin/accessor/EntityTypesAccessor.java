package org.geysermc.mappings.mixin.accessor;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import org.geysermc.mappings.definitions.util.UtilMappings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

/// Used in {@link UtilMappings}
@Mixin(EntityTypes.class)
public interface EntityTypesAccessor {

    @Accessor("OP_ONLY_CUSTOM_DATA")
    static Set<EntityType<?>> getOpOnlyCustomData() {
        throw new AssertionError();
    }
}
