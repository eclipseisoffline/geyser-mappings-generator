package org.geysermc.generator.mappings;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import org.geysermc.generator.definitions.component.ItemDataComponents;
import org.geysermc.generator.definitions.item.ItemEntry;
import org.geysermc.generator.definitions.item.RuntimeItemState;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public record FileType<T>(Path path, Codec<T> codec) {
    public static final FileType<String> MAP_COLOR = javaClass("MapColor");

    public static final FileType<Map<Holder<Biome>, Integer>> BIOME_MAPPINGS = mappings("biomes", Codec.unboundedMap(Biome.CODEC, Codec.INT.fieldOf("bedrock_id").codec()));
    public static final FileType<List<ItemDataComponents>> ITEM_DATA_COMPONENTS = mappings("item_data_components", ItemDataComponents.CODEC.listOf());
    public static final FileType<Map<Item, ItemEntry>> ITEM_MAPPINGS = mappings("items", Codec.unboundedMap(BuiltInRegistries.ITEM.byNameCodec(), ItemEntry.CODEC));

    public static final FileType<Map<String, Integer>> BIOME_ID_MAP = palette("biome_id_map", Codec.unboundedMap(Codec.STRING, Codec.INT));
    public static final FileType<List<RuntimeItemState>> RUNTIME_ITEM_STATES = palette("runtime_item_states", RuntimeItemState.CODEC.listOf());

    private static FileType<String> javaClass(String name) {
        return new FileType<>(Path.of("javaclass/" + name + ".java"), Codec.STRING);
    }

    private static <T> FileType<T> mappings(String name, Codec<T> codec) {
        return new FileType<>(Path.of("mappings/" + name + ".json"), codec);
    }

    private static <T> FileType<T> palette(String name, Codec<T> codec) {
        return new FileType<>(Path.of("palettes/" + name + ".json"), codec);
    }
}
