package org.geysermc.generator.mappings;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import org.geysermc.generator.definitions.collision.CollisionsMappings;
import org.geysermc.generator.definitions.component.ItemDataComponents;
import org.geysermc.generator.definitions.item.ItemEntry;
import org.geysermc.generator.definitions.item.RuntimeItemState;
import org.geysermc.generator.definitions.mcpl.NetworkCodec;
import org.geysermc.generator.definitions.mcpl.NetworkTags;
import org.geysermc.generator.definitions.particle.ParticleMapping;
import org.geysermc.generator.definitions.util.UtilMappings;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public record FileType<T>(Path path, Codec<T> codec) {
    public static final FileType<String> MAP_COLOR = javaClass("MapColor");

    public static final FileType<String> MCPL_BLOCK_EVENT = javaClass("ClientboundBlockEventPacket").parented("mcpl");
    public static final FileType<String> MCPL_BUILTIN_SOUND = javaClass("BuiltinSound").parented("mcpl");
    public static final FileType<String> MCPL_CUSTOM_STATISTIC = javaClass("CustomStatistic").parented("mcpl");
    public static final FileType<NetworkCodec> MCPL_NETWORK_CODEC = nbtData("networkCodec", NetworkCodec.CODEC).parented("mcpl");
    public static final FileType<NetworkTags> MCPL_NETWORK_TAGS = nbtData("networkTags", NetworkTags.CODEC).parented("mcpl");

    public static final FileType<Map<Holder<Biome>, Integer>> BIOME_MAPPINGS = jsonMappings("biomes", Codec.unboundedMap(Biome.CODEC, Codec.INT.fieldOf("bedrock_id").codec()));
    public static final FileType<CollisionsMappings> COLLISION_MAPPINGS = nbtMappings("collisions", CollisionsMappings.CODEC);
    public static final FileType<List<ItemDataComponents>> ITEM_DATA_COMPONENTS = jsonMappings("item_data_components", ItemDataComponents.CODEC.listOf());
    public static final FileType<Map<Item, ItemEntry>> ITEM_MAPPINGS = jsonMappings("items", Codec.unboundedMap(BuiltInRegistries.ITEM.byNameCodec(), ItemEntry.CODEC));
    public static final FileType<Map<String, ParticleMapping>> PARTICLE_MAPPINGS = jsonMappings("particles", Codec.unboundedMap(Codec.STRING, ParticleMapping.CODEC));
    public static final FileType<UtilMappings> UTIL_MAPPINGS = jsonMappings("util", UtilMappings.CODEC);

    public static final FileType<Map<String, Integer>> BIOME_ID_MAP = jsonPalette("biome_id_map", Codec.unboundedMap(Codec.STRING, Codec.INT));
    public static final FileType<List<RuntimeItemState>> RUNTIME_ITEM_STATES = jsonPalette("runtime_item_states", RuntimeItemState.CODEC.listOf());

    private FileType<T> parented(String parent) {
        return new FileType<>(Path.of(parent).resolve(path), codec);
    }

    private static FileType<String> javaClass(String name) {
        return new FileType<>(Path.of("javaclass/" + name + ".java"), Codec.STRING);
    }

    private static <T> FileType<T> nbtData(String name, Codec<T> codec) {
        return new FileType<>(Path.of("data/" + name + ".nbt"), codec);
    }

    private static <T> FileType<T> jsonMappings(String name, Codec<T> codec) {
        return new FileType<>(Path.of("mappings/" + name + ".json"), codec);
    }

    private static <T> FileType<T> nbtMappings(String name, Codec<T> codec) {
        return new FileType<>(Path.of("mappings/" + name + ".nbt"), codec);
    }

    private static <T> FileType<T> jsonPalette(String name, Codec<T> codec) {
        return new FileType<>(Path.of("palettes/" + name + ".json"), codec);
    }
}
