package org.geysermc.generator.definitions.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record RuntimeItemStates(Map<Identifier, State> states) {
    public static final Codec<RuntimeItemStates> CODEC = State.CODEC.listOf()
            .xmap(states -> states.stream().collect(Collectors.toUnmodifiableMap(state -> state.name, Function.identity())),
                    states -> states.values().stream().toList())
            .xmap(RuntimeItemStates::new, RuntimeItemStates::states);

    public record State(Identifier name, int id, int version, boolean componentBased) {
        public static final Codec<State> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Identifier.CODEC.fieldOf("name").forGetter(State::name),
                        Codec.INT.fieldOf("id").forGetter(State::id),
                        Codec.INT.fieldOf("version").forGetter(State::version),
                        Codec.BOOL.fieldOf("componentBased").forGetter(State::componentBased)
                ).apply(instance, State::new)
        );
    }
}
