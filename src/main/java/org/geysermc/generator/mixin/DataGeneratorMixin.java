package org.geysermc.generator.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import org.geysermc.generator.mappings.MappingsOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.Path;

@Mixin(DataGenerator.class)
public abstract class DataGeneratorMixin {

    @Mixin(DataGenerator.Uncached.class)
    public static abstract class Uncached extends DataGenerator {
        @Unique
        private static final ScopedValue<MappingsOutput> output = ScopedValue.newInstance();

        public Uncached(Path output) {
            super(output);
        }

        @WrapMethod(method = "run")
        public void openMappingsOutput(Operation<Void> original) {
            Path outputFolder = vanillaPackOutput.getOutputFolder();
            try (MappingsOutput openedOutput = MappingsOutput.open(() -> outputFolder, CachedOutput.NO_CACHE).join()) {
                ScopedValue.where(output, openedOutput).run(original::call);
            }
        }

        @Definition(id = "NO_CACHE", field = "Lnet/minecraft/data/CachedOutput;NO_CACHE:Lnet/minecraft/data/CachedOutput;")
        @Expression("NO_CACHE")
        @ModifyExpressionValue(method = "lambda$run$0", at = @At("MIXINEXTRAS:EXPRESSION"))
        private static CachedOutput returnMappingsOutput(CachedOutput cache) {
            return output.get();
        }
    }
}
