package org.geysermc.generator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.InstrumentComponent;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/// Resolvable data components are data components holding a data-driven value. We export the default values of these components so that they can be loaded
/// by Geyser when a session finishes configuration.
///
/// Components holding a simple `Holder<T>` or `HolderSet<T>` are automatically detected and mapped,
/// Other resolvable components have to be added manually to `CUSTOM_RESOLVABLE_COMPONENTS`.
///
/// When looking for new resolvable component types, look for the following qualities:
/// - A component type holding a "wrapper record" that holds a single data-driven holder: these are easy to map, see e.g. `INSTRUMENT` or `JUKEBOX_PLAYABLE`.
/// - The same, but for a data-driven HolderSet: see e.g. `DAMAGE_RESISTANT`.
/// - More complicated components that somehow hold a data-driven element, through a Holder or HolderSet: these are more complicated to map. Currently, we don't have to deal with these however! (see description at `CUSTOM_RESOLVABLE_COMPONENTS`)
/// Note that components only have to be mapped if they're used as default item components. You can check this easily by seeing if the component type is used in the vanilla Items class.
public final class ResolvableDataComponentGenerator {
    // Components which do not allow direct serialisation over network and instead are only ever encoded using network IDs
    // Can't automatically detect this until we have mixins
    private static final List<DataComponentType<?>> WALL_OF_SHAME = List.of(DataComponents.CHICKEN_VARIANT, DataComponents.DAMAGE_TYPE);
    // Not translating enchantments/stored enchantments: as far as I know, there are no items with enchantments built-in
    // Not translating trims: as far as I know, there are no items with trims built-in
    // Not translating banner patterns: all usages in Items have no layers?
    private static final List<ResolvableDataComponentType<?>> CUSTOM_RESOLVABLE_COMPONENTS = List.of(
            ResolvableDataComponentType.ofHolderSet(DataComponents.DAMAGE_RESISTANT, DamageResistant::types),
            ResolvableDataComponentType.ofHolder(DataComponents.INSTRUMENT, InstrumentComponent::instrument, true),
            ResolvableDataComponentType.ofHolder(DataComponents.JUKEBOX_PLAYABLE, JukeboxPlayable::song, true),
            // Manually adding these 2: they are not detected automatically, because they use .cacheEncoding, which wraps their codec and makes it unable to detect
            // (until we have mixins)
            ResolvableDataComponentType.ofHolder(DataComponents.PROVIDES_TRIM_MATERIAL),
            ResolvableDataComponentType.ofHolderSet(DataComponents.PROVIDES_BANNER_PATTERNS)
    );

    public static void generate() {
        List<ResolvableDataComponentType<?>> resolvableDataComponentTypes = new ArrayList<>(CUSTOM_RESOLVABLE_COMPONENTS);
        for (DataComponentType<?> componentType : BuiltInRegistries.DATA_COMPONENT_TYPE) {
            Codec<?> valueCodec = componentType.codec();
            if (valueCodec instanceof RegistryFixedCodec<?> || valueCodec instanceof RegistryFileCodec<?>) {
                // These usually have Holders as values - if not then it'll just throw, and we can add an exception for it
                resolvableDataComponentTypes.add(ResolvableDataComponentType.ofHolder((DataComponentType) componentType));
            } else if (valueCodec instanceof HolderSetCodec<?>) {
                // Same as above
                resolvableDataComponentTypes.add(ResolvableDataComponentType.ofHolderSet((DataComponentType) componentType));
            }
        }

        System.out.println("Mapping resolvable component types: " + resolvableDataComponentTypes.stream().map(ResolvableDataComponentType::type).toList());

        List<List<TypedResolvableDataComponent>> allResolvableDataComponents = new ArrayList<>();
        BuiltInRegistries.ITEM.forEach(item -> {
            DataComponentMap components = item.components();
            List<TypedResolvableDataComponent> resolvableDataComponents = new ArrayList<>();
            for (ResolvableDataComponentType<?> resolvable : resolvableDataComponentTypes) {
                resolvable.get(components).ifPresent(resolvableDataComponents::add);
            }
            allResolvableDataComponents.add(resolvableDataComponents);
        });

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
        try {
            Files.writeString(Path.of("mappings/resolvable_item_data_components.json"), gson.toJson(
                    TypedResolvableDataComponent.CODEC.listOf().listOf().encodeStart(JsonOps.INSTANCE, allResolvableDataComponents)
            ));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private interface ResolvableDataComponent {

        MapCodec<ResolvableDataComponent> MAP_CODEC = Type.CODEC.dispatchMap(ResolvableDataComponent::type, type -> type.codec);

        Type type();

        enum Type implements StringRepresentable {
            HOLDER("holder", HolderReferenceComponent.MAP_CODEC),
            HOLDER_SET("holder_set", HolderSetComponent.MAP_CODEC);

            public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

            private final String name;
            private final MapCodec<? extends ResolvableDataComponent> codec;

            Type(String name, MapCodec<? extends ResolvableDataComponent> codec) {
                this.name = name;
                this.codec = codec;
            }

            @Override
            public @NonNull String getSerializedName() {
                return name;
            }
        }
    }

    private record HolderReferenceComponent(Identifier registry, Identifier reference, boolean allowsDirectOverNetwork) implements ResolvableDataComponent {
        public static final MapCodec<HolderReferenceComponent> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Identifier.CODEC.fieldOf("registry").forGetter(HolderReferenceComponent::registry),
                        Identifier.CODEC.fieldOf("reference").forGetter(HolderReferenceComponent::reference),
                        Codec.BOOL.fieldOf("allows_direct_over_network").forGetter(HolderReferenceComponent::allowsDirectOverNetwork)
                ).apply(instance, HolderReferenceComponent::new)
        );

        private HolderReferenceComponent(Holder.Reference<?> holder, boolean allowsDirectOverNetwork) {
            this(holder.key().registry(), holder.key().identifier(), allowsDirectOverNetwork);
        }

        @Override
        public Type type() {
            return Type.HOLDER;
        }
    }

    private record HolderSetComponent(Optional<Identifier> registry, List<Identifier> references) implements ResolvableDataComponent {
        public static final MapCodec<HolderSetComponent> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Identifier.CODEC.optionalFieldOf("registry").forGetter(HolderSetComponent::registry),
                        Identifier.CODEC.listOf().optionalFieldOf("references", List.of()).forGetter(HolderSetComponent::references)
                ).apply(instance, HolderSetComponent::new)
        );

        @Override
        public Type type() {
            return Type.HOLDER_SET;
        }
    }

    private record ResolvableDataComponentType<T>(DataComponentType<T> type, Function<T, Optional<ResolvableDataComponent>> dataGetter) {

        private static <T, R extends ResolvableDataComponent> ResolvableDataComponentType<T> of(DataComponentType<T> type, Function<T, R> dataGetter) {
            return new ResolvableDataComponentType<>(type, dataGetter.andThen(Optional::of));
        }

        private static <T, R> ResolvableDataComponentType<T> ofHolder(DataComponentType<T> type, Function<T, Holder<R>> toHolder, boolean allowsDirectOverNetwork) {
            return new ResolvableDataComponentType<>(type, value -> {
                Holder<R> holder = toHolder.apply(value);
                if (holder instanceof Holder.Reference<R> reference) {
                    return Optional.of(new HolderReferenceComponent(reference, allowsDirectOverNetwork));
                }
                // Direct holders are encoded, well, directly, and loaded correctly by the base default component loader
                return Optional.empty();
            });
        }

        private static <T> ResolvableDataComponentType<Holder<T>> ofHolder(DataComponentType<Holder<T>> type) {
            return ofHolder(type, Function.identity(), !WALL_OF_SHAME.contains(type));
        }

        private static <T, R> ResolvableDataComponentType<T> ofHolderSet(DataComponentType<T> type, Function<T, HolderSet<R>> toHolderSet) {
            return new ResolvableDataComponentType<>(type, value -> {
                HolderSet<R> holders = toHolderSet.apply(value);
                if (holders instanceof HolderSet.Direct<R> direct) {
                    List<Holder<R>> contents = direct.unwrap().right().orElseThrow();
                    if (contents.isEmpty()) {
                        return Optional.of(new HolderSetComponent(Optional.empty(), List.of()));
                    }
                    List<Holder.Reference<R>> references = contents.stream()
                            // We only support references here. If there's any occassion where a holder is not a reference, then, we'll have to implement a fix!
                            .map(holder -> (Holder.Reference<R>) holder)
                            .toList();
                    return Optional.of(new HolderSetComponent(Optional.of(references.getFirst().key().registry()), references.stream().map(reference -> reference.key().identifier()).toList()));
                }
                // Named holder sets are tags which the base default component loader loads correctly
                return Optional.empty();
            });
        }

        private static <T> ResolvableDataComponentType<HolderSet<T>> ofHolderSet(DataComponentType<HolderSet<T>> type) {
            return ofHolderSet(type, Function.identity());
        }

        private Optional<TypedResolvableDataComponent> get(DataComponentMap components) {
            T value = components.get(type);
            if (value == null) {
                return Optional.empty();
            }
            return dataGetter.apply(value).map(data -> new TypedResolvableDataComponent(type, data));
        }
    }

    private record TypedResolvableDataComponent(DataComponentType<?> type, ResolvableDataComponent value) {
        public static final Codec<TypedResolvableDataComponent> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        DataComponentType.PERSISTENT_CODEC.fieldOf("component").forGetter(TypedResolvableDataComponent::type),
                        ResolvableDataComponent.MAP_CODEC.forGetter(TypedResolvableDataComponent::value)
                ).apply(instance, TypedResolvableDataComponent::new)
        );
    }
}
