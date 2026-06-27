package org.geysermc.generator;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class DataComponentGenerator implements DataProvider {
    private static final Path OUTPUT = Path.of("item_data_components.json");

    private final Path output;
    private final CompletableFuture<RegistryAccess> registries;

    public DataComponentGenerator(PackOutput output, CompletableFuture<RegistryAccess> registries) {
        this.output = output.getOutputFolder().resolve(OUTPUT);
        this.registries = registries;
    }

    // ripped from https://github.com/AlexProgrammerDE/SoulFire/blob/9b0280b2bca76aa234a2283bf4ab82300150cef6/data-generator/src/main/java/com/soulfiremc/generator/generators/ItemsDataGenerator.java#L43-L58
    // thanks pistonmaster for the permission to use it!
    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return registries.thenCompose(registries -> {
            var allItemComponents = new JsonArray();
            BuiltInRegistries.ITEM.forEach(
                    item -> {
                        JsonObject entry = new JsonObject();

                        entry.addProperty("id", BuiltInRegistries.ITEM.getId(item));
                        entry.addProperty("key", BuiltInRegistries.ITEM.getKey(item).toString());

                        var sortedComponentObj = new JsonObject();
                        item.components().stream()
                                .map(typed -> {
                                    ByteBuf buf = Unpooled.buffer();
                                    RegistryFriendlyByteBuf registryBuf = new RegistryFriendlyByteBuf(buf, registries);
                                    registryBuf.writeVarInt(BuiltInRegistries.DATA_COMPONENT_TYPE.getId(typed.type()));
                                    writeComponent(registryBuf, typed);
                                    byte[] bytes = new byte[buf.readableBytes()];
                                    buf.readBytes(bytes);
                                    buf.release();
                                    String data = Base64.getEncoder().encodeToString(bytes);
                                    return Map.entry(
                                            Objects.requireNonNull(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(typed.type())).toString(),
                                            data);
                                })
                                .sorted(Map.Entry.comparingByKey())
                                .forEach(e -> sortedComponentObj.add(e.getKey(), new JsonPrimitive(e.getValue())));
                        entry.add("components", sortedComponentObj);

                        allItemComponents.add(entry);
                    }
            );

            return DataProvider.saveStable(cache, allItemComponents, output);
        });
    }

    @Override
    public String getName() {
        return "Default Data Component Generator";
    }

    private static <T> void writeComponent(RegistryFriendlyByteBuf buf, TypedDataComponent<T> typed) {
        typed.type().streamCodec().encode(buf, typed.value());
    }
}
