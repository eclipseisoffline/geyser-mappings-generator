package org.geysermc.mappings.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import net.minecraft.data.DataProvider;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

/// Used to sort {@link CompoundTag}s before writing them to disk, so that the output is consistent when no code has changed
@Mixin(CompoundTag.class)
public abstract class CompoundTagMixin {

    @ModifyExpressionValue(method = "write", at = @At(value = "INVOKE", target = "Ljava/util/Map;keySet()Ljava/util/Set;"))
    public Set<String> sortKeySetBeforeWriting(Set<String> original) {
        Set<String> sorted = new ObjectAVLTreeSet<>(DataProvider.KEY_COMPARATOR);
        sorted.addAll(original);
        return sorted;
    }
}
