package org.geysermc.mappings.mixin.accessor;

import net.minecraft.stats.Stat;
import net.minecraft.stats.StatFormatter;
import org.geysermc.mappings.generator.mcpl.CustomStatisticGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/// Used in {@link CustomStatisticGenerator}
@Mixin(Stat.class)
public interface StatAccessor {

    @Accessor
    StatFormatter getFormatter();
}
