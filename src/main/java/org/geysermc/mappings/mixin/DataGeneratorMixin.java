package org.geysermc.mappings.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import org.geysermc.mappings.MappingsOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;

/// Used to use {@link MappingsOutput} as {@link CachedOutput} for all {@link DataProvider}s
@Mixin(DataGenerator.class)
public abstract class DataGeneratorMixin {

    @Mixin(DataGenerator.Cached.class)
    public static abstract class Cached extends DataGenerator {
        @Unique
        private static final ScopedValue<MappingsOutput> output = ScopedValue.newInstance();

        public Cached(Path output) {
            super(output);
        }

        @WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
        public void openMappingsOutput(Map<String, DataProvider> instance, BiConsumer<? super String, ? super DataProvider> action, Operation<Void> original) {
            Path outputFolder = vanillaPackOutput.getOutputFolder();
            try (MappingsOutput openedOutput = MappingsOutput.open(() -> outputFolder).join()) {
                ScopedValue.where(output, openedOutput).run(() -> original.call(instance, action));
            }
        }

        @Definition(id = "provider", local = @Local(type = DataProvider.class, name = "provider", argsOnly = true))
        @Definition(id = "run", method = "Lnet/minecraft/data/DataProvider;run(Lnet/minecraft/data/CachedOutput;)Ljava/util/concurrent/CompletableFuture;")
        @Expression("provider::run")
        @ModifyExpressionValue(method = "lambda$run$0", at = @At("MIXINEXTRAS:EXPRESSION"))
        private static HashCache.UpdateFunction returnUpdateFunctionWithMappingsOutput(HashCache.UpdateFunction original) {
            return delegate -> original.update(output.get().createOutput(delegate));
        }
    }

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
            try (MappingsOutput openedOutput = MappingsOutput.open(() -> outputFolder).join()) {
                ScopedValue.where(output, openedOutput).run(original::call);
            }
        }

        @Definition(id = "NO_CACHE", field = "Lnet/minecraft/data/CachedOutput;NO_CACHE:Lnet/minecraft/data/CachedOutput;")
        @Expression("NO_CACHE")
        @ModifyExpressionValue(method = "lambda$run$0", at = @At("MIXINEXTRAS:EXPRESSION"))
        private static CachedOutput returnMappingsOutput(CachedOutput delegate) {
            return output.get().createOutput(delegate);
        }
    }
}
