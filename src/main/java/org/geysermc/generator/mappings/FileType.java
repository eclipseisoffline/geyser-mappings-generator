package org.geysermc.generator.mappings;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import org.geysermc.generator.definitions.block.BlockEntry;
import org.geysermc.generator.definitions.block.BlockMappings;
import org.geysermc.generator.definitions.block.BlockPalette;
import org.geysermc.generator.definitions.collision.CollisionsMappings;
import org.geysermc.generator.definitions.component.ItemDataComponents;
import org.geysermc.generator.definitions.item.ItemComponents;
import org.geysermc.generator.definitions.item.ItemEntry;
import org.geysermc.generator.definitions.item.RuntimeItemStates;
import org.geysermc.generator.definitions.mcpl.NetworkCodec;
import org.geysermc.generator.definitions.mcpl.NetworkTags;
import org.geysermc.generator.definitions.particle.ParticleMapping;
import org.geysermc.generator.definitions.util.UtilMappings;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public record FileType<T>(Path path, Codec<T> codec, Type type) {
    public static final FileType<String> MAP_COLOR = javaClass("MapColor");

    public static final FileType<String> MCPL_BLOCK_EVENT = javaClass("ClientboundBlockEventPacket").parented("mcpl");
    public static final FileType<String> MCPL_BUILTIN_SOUND = javaClass("BuiltinSound").parented("mcpl");
    public static final FileType<String> MCPL_CUSTOM_STATISTIC = javaClass("CustomStatistic").parented("mcpl");
    public static final FileType<NetworkCodec> MCPL_NETWORK_CODEC = nbtData("networkCodec", NetworkCodec.CODEC).parented("mcpl");
    public static final FileType<NetworkTags> MCPL_NETWORK_TAGS = nbtData("networkTags", NetworkTags.CODEC).parented("mcpl");

    public static final FileType<Map<BlockState, BlockEntry>> BLOCK_MAPPINGS_DEBUG = jsonData("blocks_debug", BlockMappings.DEBUG_CODEC);

    public static final FileType<List<Identifier>> ADDITIONAL_OFFHAND_ITEMS = jsonMappings("additional_offhand_items", Identifier.CODEC.listOf());
    public static final FileType<Map<Holder<Biome>, Integer>> BIOME_MAPPINGS = jsonMappings("biomes", Codec.unboundedMap(Biome.CODEC, Codec.INT.fieldOf("bedrock_id").codec()));
    public static final FileType<Map<BlockState, BlockEntry>> BLOCK_MAPPINGS = nbtMappings("blocks", BlockMappings.CODEC);
    public static final FileType<CollisionsMappings> COLLISION_MAPPINGS = nbtMappings("collisions", CollisionsMappings.CODEC);
    public static final FileType<List<ItemDataComponents>> ITEM_DATA_COMPONENTS = jsonMappings("item_data_components", ItemDataComponents.CODEC.listOf());
    public static final FileType<Map<Identifier, CompoundTag>> ITEM_COMPONENTS = nbtMappings("item_components", ItemComponents.COMPONENTS_CODEC);
    public static final FileType<Map<Item, ItemEntry>> ITEM_MAPPINGS = jsonMappings("items", Codec.unboundedMap(BuiltInRegistries.ITEM.byNameCodec(), ItemEntry.CODEC));
    public static final FileType<Map<String, ParticleMapping>> PARTICLE_MAPPINGS = jsonMappings("particles", Codec.unboundedMap(Codec.STRING, ParticleMapping.CODEC));
    public static final FileType<UtilMappings> UTIL_MAPPINGS = jsonMappings("util", UtilMappings.CODEC);

    public static final FileType<Map<String, Integer>> BIOME_ID_MAP = jsonPalette("biome_id_map", Codec.unboundedMap(Codec.STRING, Codec.INT));
    public static final FileType<BlockPalette> BLOCK_PALETTE = nbtPalette("block_palette", BlockPalette.CODEC);
    public static final FileType<Map<Identifier, CompoundTag>> ITEM_COMPONENTS_PALETTE = nbtPalette("item_components", ItemComponents.COMPONENTS_CODEC);
    public static final FileType<RuntimeItemStates> RUNTIME_ITEM_STATES = jsonPalette("runtime_item_states", RuntimeItemStates.CODEC);

    private FileType<T> parented(String parent) {
        return new FileType<>(Path.of(parent).resolve(path), codec, type);
    }

    private static FileType<String> javaClass(String name) {
        return new FileType<>(Path.of("javaclass/" + name + ".java"), Codec.STRING, Type.TEXT);
    }

    private static <T> FileType<T> jsonData(String name, Codec<T> codec) {
        return new FileType<>(Path.of("data/" + name + ".json"), codec, Type.JSON);
    }

    private static <T> FileType<T> nbtData(String name, Codec<T> codec) {
        return new FileType<>(Path.of("data/" + name + ".nbt"), codec, Type.NBT);
    }

    private static <T> FileType<T> jsonMappings(String name, Codec<T> codec) {
        return new FileType<>(Path.of("mappings/" + name + ".json"), codec, Type.JSON);
    }

    private static <T> FileType<T> nbtMappings(String name, Codec<T> codec) {
        return new FileType<>(Path.of("mappings/" + name + ".nbt"), codec, Type.NBT);
    }

    private static <T> FileType<T> jsonPalette(String name, Codec<T> codec) {
        return new FileType<>(Path.of("palettes/" + name + ".json"), codec, Type.JSON);
    }

    private static <T> FileType<T> nbtPalette(String name, Codec<T> codec) {
        return new FileType<>(Path.of("palettes/" + name + ".nbt"), codec, Type.NBT);
    }

    public enum Type {
        JSON,
        NBT,
        TEXT
    }
}
