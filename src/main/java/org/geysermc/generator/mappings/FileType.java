package org.geysermc.generator.mappings;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import org.geysermc.generator.definitions.item.ItemEntry;
import org.geysermc.generator.definitions.item.RuntimeItemState;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public record FileType<T>(Path path, Codec<T> codec) {
    public static final FileType<Map<Holder<Item>, ItemEntry>> ITEM_MAPPINGS = mappings("items", Codec.unboundedMap(Item.CODEC, ItemEntry.CODEC));

    public static final FileType<List<RuntimeItemState>> RUNTIME_ITEM_STATES = palette("runtime_item_states", RuntimeItemState.CODEC.listOf());

    private static <T> FileType<T> mappings(String name, Codec<T> codec) {
        return new FileType<>(Path.of("mappings/" + name + ".json"), codec);
    }

    private static <T> FileType<T> palette(String name, Codec<T> codec) {
        return new FileType<>(Path.of("palettes/" + name + ".json"), codec);
    }
}
