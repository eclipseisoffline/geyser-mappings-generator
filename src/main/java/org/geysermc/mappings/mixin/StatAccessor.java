package org.geysermc.mappings.mixin;

import net.minecraft.stats.Stat;
import net.minecraft.stats.StatFormatter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Stat.class)
public interface StatAccessor {

    @Accessor
    StatFormatter getFormatter();
}
