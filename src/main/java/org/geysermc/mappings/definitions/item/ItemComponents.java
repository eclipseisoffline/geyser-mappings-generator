package org.geysermc.mappings.definitions.item;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import org.geysermc.mappings.FileType;
import org.geysermc.mappings.MappingsAccess;
import org.geysermc.mappings.util.MappingsUtil;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public record ItemComponents(RuntimeItemStates states, Map<Identifier, CompoundTag> components) {
    public static final Codec<Map<Identifier, CompoundTag>> COMPONENTS_CODEC = Codec.unboundedMap(Identifier.CODEC, CompoundTag.CODEC.optionalFieldOf("components", MappingsUtil.EMPTY_TAG).codec());

    public static CompletableFuture<ItemComponents> open(MappingsAccess access) {
        return access.readFileOrThrow(FileType.RUNTIME_ITEM_STATES).thenCombine(access.readFileOrThrow(FileType.ITEM_COMPONENTS_PALETTE), ItemComponents::new);
    }
}
