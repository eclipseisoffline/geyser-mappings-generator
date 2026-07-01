package org.geysermc.generator.definitions.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

public record ItemDataComponents(int id, Identifier identifier, Map<DataComponentType<?>, String> components) {
    public static final Codec<ItemDataComponents> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("id").forGetter(ItemDataComponents::id),
                    Identifier.CODEC.fieldOf("key").forGetter(ItemDataComponents::identifier),
                    Codec.unboundedMap(DataComponentType.CODEC, Codec.STRING).fieldOf("components").forGetter(ItemDataComponents::components)
            ).apply(instance, ItemDataComponents::new)
    );

    // ripped from https://github.com/AlexProgrammerDE/SoulFire/blob/9b0280b2bca76aa234a2283bf4ab82300150cef6/data-generator/src/main/java/com/soulfiremc/generator/generators/ItemsDataGenerator.java#L43-L58
    // thanks pistonmaster for the permission to use it!
    public static ItemDataComponents create(Item item, RegistryAccess registries) {
        int id = BuiltInRegistries.ITEM.getId(item);
        Identifier key = BuiltInRegistries.ITEM.getKey(item);

        Map<DataComponentType<?>, String> components = item.components().stream()
                .map(typed -> {
                    ByteBuf buf = Unpooled.buffer();
                    RegistryFriendlyByteBuf registryBuf = new RegistryFriendlyByteBuf(buf, registries);
                    registryBuf.writeVarInt(BuiltInRegistries.DATA_COMPONENT_TYPE.getId(typed.type()));
                    writeComponent(registryBuf, typed);
                    byte[] bytes = new byte[buf.readableBytes()];
                    buf.readBytes(bytes);
                    buf.release();
                    String data = Base64.getEncoder().encodeToString(bytes);
                    return Map.entry(typed.type(), data);
                })
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        return new ItemDataComponents(id, key, components);
    }

    private static <T> void writeComponent(RegistryFriendlyByteBuf buf, TypedDataComponent<T> typed) {
        typed.type().streamCodec().encode(buf, typed.value());
    }
}
