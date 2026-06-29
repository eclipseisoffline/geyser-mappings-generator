package org.geysermc.generator.definitions.mcpl;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record NetworkCodec(Map<ResourceKey<? extends Registry<?>>, RegistryData<?>> registries) {
    public static final Codec<ResourceKey<? extends Registry<?>>> REGISTRY_KEY_CODEC = Identifier.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::identifier);
    public static final Codec<NetworkCodec> CODEC = Codec.unboundedMap(REGISTRY_KEY_CODEC, RegistryData.CODEC).xmap(NetworkCodec::new, NetworkCodec::registries);

    public static NetworkCodec collect(RegistryAccess registries) {
        return new NetworkCodec(RegistryDataLoader.SYNCHRONIZED_REGISTRIES.stream()
                .map(data -> Pair.of(data.key(), RegistryData.collect(registries, data.key())))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
    }

    public static <T> ResourceKey<? extends Registry<T>> castRegistryKey(ResourceKey<? extends Registry<?>> key) {
        return (ResourceKey<? extends Registry<T>>) key;
    }

    public record RegistryData<T>(ResourceKey<? extends Registry<T>> key, List<RegistryEntry<T>> entries) {
        public static final Codec<RegistryData<?>> CODEC = REGISTRY_KEY_CODEC.dispatch(RegistryData::key, RegistryData::codec);

        private static <T> RegistryData<T> collect(RegistryAccess registries, ResourceKey<? extends Registry<?>> key) {
            ResourceKey<? extends Registry<T>> casted = castRegistryKey(key);
            Registry<T> registry = registries.lookupOrThrow(casted);
            return new RegistryData<>(casted, registry.stream()
                    .map(value -> new RegistryEntry<>(registry.getKey(value), registry.getIdOrThrow(value), value))
                    .toList());
        }

        private static <T> MapCodec<RegistryData<T>> codec(ResourceKey<? extends Registry<?>> key) {
            ResourceKey<? extends Registry<T>> casted = castRegistryKey(key);
            return RegistryEntry.codec(casted).listOf().fieldOf("value")
                    .xmap(list -> new RegistryData<>(casted, list), RegistryData::entries);
        }
    }

    public record RegistryEntry<T>(Identifier name, int id, T value) {

        public static <T> Codec<RegistryEntry<T>> codec(ResourceKey<? extends Registry<T>> registry) {
            return RegistryDataLoader.SYNCHRONIZED_REGISTRIES.stream()
                    .filter(data -> data.key() == registry)
                    .findFirst()
                    .map(data -> (Codec<T>) data.elementCodec())
                    .<Codec<RegistryEntry<T>>>map(codec -> RecordCodecBuilder.create(instance ->
                            instance.group(
                                    Identifier.CODEC.fieldOf("name").forGetter(RegistryEntry::name),
                                    Codec.INT.fieldOf("id").forGetter(RegistryEntry::id),
                                    codec.fieldOf("element").forGetter(RegistryEntry::value)
                            ).apply(instance, RegistryEntry::new)
                    ))
                    .orElseThrow();
        }
    }
}
