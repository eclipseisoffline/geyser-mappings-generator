package org.geysermc.mappings.definitions.mcpl;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.Map;
import java.util.stream.Collectors;

public record NetworkTags(Map<ResourceKey<? extends Registry<?>>, RegistryTags<?>> tags) {
    public static final Codec<NetworkTags> CODEC = Codec.dispatchedMap(NetworkCodec.REGISTRY_KEY_CODEC, key -> RegistryTags.codec(NetworkCodec.castRegistryKey(key)))
            .xmap(tags -> new NetworkTags((Map) tags), tags -> (Map) tags.tags);

    public static NetworkTags collect(RegistryAccess registries) {
        return new NetworkTags(registries.listRegistryKeys()
                .filter(key -> BuiltInRegistries.REGISTRY.containsKey(key.identifier()) || RegistrySynchronization.isNetworkable(key))
                .map(registries::lookupOrThrow)
                .map(registry -> Pair.of(registry.key(), RegistryTags.collect(registry)))
                .filter(pair -> !pair.getSecond().tags.isEmpty())
                .collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)));
    }

    public record RegistryTags<T>(Map<TagKey<T>, IntList> tags) {

        private static <T> RegistryTags<T> collect(Registry<T> registry) {
            return new RegistryTags<>(registry.getTags()
                    .collect(Collectors.toUnmodifiableMap(HolderSet.Named::key, tag -> IntList.of(tag.stream()
                            .mapToInt(holder -> registry.asHolderIdMap().getIdOrThrow(holder))
                            .toArray()))));
        }

        public static <T> Codec<RegistryTags<T>> codec(ResourceKey<? extends Registry<T>> registry) {
            return Codec.unboundedMap(TagKey.codec(registry), Codec.INT_STREAM.xmap(stream -> IntList.of(stream.toArray()), IntCollection::intStream))
                    .xmap(RegistryTags::new, RegistryTags::tags);
        }
    }
}
