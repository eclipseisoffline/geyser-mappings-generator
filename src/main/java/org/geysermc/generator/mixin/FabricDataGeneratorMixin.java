package org.geysermc.generator.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.DataGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.Path;

/// Cancels FabricDataGenerator deleting our old mappings
@Mixin(FabricDataGenerator.class)
public abstract class FabricDataGeneratorMixin extends DataGenerator.Uncached {

    public FabricDataGeneratorMixin(Path output) {
        super(output);
    }

    @WrapWithCondition(method = "run", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/datagen/FabricDataGenHelper;deleteDirectory(Ljava/nio/file/Path;)V"))
    public boolean neverDeleteMappings(Path dir) {
        return false;
    }
}
