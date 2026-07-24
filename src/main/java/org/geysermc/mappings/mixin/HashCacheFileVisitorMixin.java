package org.geysermc.mappings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/// Used to cancel HashCache deleting our files, we manage that ourselves in MappingsOutput
@Mixin(targets = "net.minecraft.data.HashCache$1")
public abstract class HashCacheFileVisitorMixin {

    @Inject(method = "visitFile(Ljava/nio/file/Path;Ljava/nio/file/attribute/BasicFileAttributes;)Ljava/nio/file/FileVisitResult;", at = @At(value = "INVOKE", target = "Ljava/nio/file/Files;delete(Ljava/nio/file/Path;)V"), cancellable = true)
    public void cancelDelete(Path file, BasicFileAttributes attrs, CallbackInfoReturnable<FileVisitResult> callbackInfoReturnable) {
        callbackInfoReturnable.setReturnValue(FileVisitResult.CONTINUE);
    }
}
