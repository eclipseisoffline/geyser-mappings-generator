package org.geysermc.generator.mixin;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DataGenerator.PackGenerator.class)
public interface PackGeneratorAccessor {

    @Accessor
    PackOutput getOutput();
}
