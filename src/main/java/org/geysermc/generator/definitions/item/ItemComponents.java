package org.geysermc.generator.definitions.item;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import org.geysermc.generator.mappings.FileType;
import org.geysermc.generator.mappings.MappingsAccess;
import org.geysermc.generator.util.MappingsUtil;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public record ItemComponents(RuntimeItemStates states, Map<Identifier, CompoundTag> components) {
    public static final Codec<Map<Identifier, CompoundTag>> COMPONENTS_CODEC = Codec.unboundedMap(Identifier.CODEC, CompoundTag.CODEC.optionalFieldOf("components", MappingsUtil.EMPTY_TAG).codec());

    public static CompletableFuture<ItemComponents> open(MappingsAccess access) {
        return access.readFile(FileType.RUNTIME_ITEM_STATES).thenCombine(access.readFile(FileType.ITEM_COMPONENTS_PALETTE), ItemComponents::new);
    }
}
