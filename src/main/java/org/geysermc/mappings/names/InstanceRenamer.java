package org.geysermc.mappings.names;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@FunctionalInterface
public interface InstanceRenamer<T, I, R> {

    Function<I, R> forType(T type);

    static <T, I, R> InstanceRenamer<T, I, R> of(Function<I, R> fallback, Consumer<Builder<T, I, R>> builder) {
        return of((_, instance) -> fallback.apply(instance), builder);
    }

    static <T, I, R> InstanceRenamer<T, I, R> of(BiFunction<T, I, R> fallback, Consumer<Builder<T, I, R>> builder) {
        Builder<T, I, R> renamer = new Builder<>(fallback);
        builder.accept(renamer);
        return renamer.build();
    }

    @FunctionalInterface
    interface Aggregator<T, I, R> {

        default <M> Aggregator<M, I, R> mapType(Function<M, T> mapper) {
            return new Aggregator<>() {
                @Override
                public Aggregator<M, I, R> rename(M type, Function<I, R> renamer) {
                    Aggregator.this.rename(mapper.apply(type), renamer);
                    return this;
                }
            };
        }

        default <M> Aggregator<T, M, R> mapInstance(Function<I, M> mapper) {
            return new Aggregator<>() {
                @Override
                public Aggregator<T, M, R> rename(T type, Function<M, R> renamer) {
                    Aggregator.this.rename(type, mapper.andThen(renamer));
                    return this;
                }
            };
        }

        default <M> Aggregator<T, I, M> mapResult(Function<M, R> mapper) {
            return new Aggregator<>() {
                @Override
                public Aggregator<T, I, M> rename(T type, Function<I, M> renamer) {
                    Aggregator.this.rename(type, renamer.andThen(mapper));
                    return this;
                }
            };
        }

        default Aggregator<T, I, R> rename(T type, R to) {
            return rename(type, _ -> to);
        }

        Aggregator<T, I, R> rename(T type, Function<I, R> renamer);
    }

    class Builder<T, I, R> implements Aggregator<T, I, R> {
        protected final BiFunction<T, I, R> fallback;
        protected final Map<T, Function<I, R>> renamers = new Object2ObjectOpenHashMap<>();

        public Builder(BiFunction<T, I, R> fallback) {
            this.fallback = fallback;
        }

        @Override
        public Builder<T, I, R> rename(T type, Function<I, R> renamer) {
            if (renamers.containsKey(type)) {
                throw new IllegalArgumentException("Already registered a renamer for " + type);
            }
            renamers.put(type, renamer);
            return this;
        }

        public InstanceRenamer<T, I, R> build() {
            return type -> renamers.getOrDefault(type, instance -> fallback.apply(type, instance));
        }
    }
}
