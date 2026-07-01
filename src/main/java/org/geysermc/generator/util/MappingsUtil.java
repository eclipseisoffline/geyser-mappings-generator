package org.geysermc.generator.util;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.JavaOps;
import net.minecraft.client.data.models.blockstates.PropertyValueList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.logging.log4j.core.pattern.AnsiEscape;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Stream;

public final class MappingsUtil {
    public static final CompoundTag EMPTY_TAG = new CompoundTag();

    public static Stream<Field> listPublicConstants(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()));
    }

    public static <T> T getConstant(Class<?> clazz, Field field) {
        return getConstant(clazz, field, Field::get);
    }

    public static <T> T getConstant(Class<?> clazz, Field field, FieldGetter getter) {
        return runReflectionSafely(clazz, () -> (T) getter.get(field, null));
    }

    public static <T> T runReflectionSafely(Class<?> clazz, ReflectionOperation<T> operation) {
        try {
            return operation.run();
        } catch (IllegalAccessException exception) {
            throw new RuntimeException("Failed to run reflection operation for class " + clazz.getSimpleName(), exception);
        }
    }

    public static String wrapInQuotes(String string) {
        return "\"" + string + "\"";
    }

    public static String formatString(String string, AnsiEscape... styles) {
        return AnsiEscape.createSequence(Arrays.stream(styles).map(AnsiEscape::name).toArray(String[]::new)) + string + AnsiEscape.getDefaultStyle();
    }

    public static DataResult<BlockState> blockStateFromString(String string) {
        String[] split = string.split("\\[");
        String identifier = split[0];
        DataResult<BlockState> result = BuiltInRegistries.BLOCK.byNameCodec().parse(JavaOps.INSTANCE, identifier).map(Block::defaultBlockState);
        if (split.length == 1) {
            return result;
        }

        String[] properties = split[1].split(",");
        properties[properties.length - 1] = properties[properties.length - 1].replaceFirst("]$", "");
        for (String property : properties) {
            String[] propertySplit = property.split("=");
            if (propertySplit.length != 2) {
                return DataResult.error(() -> "Unable to parse property: " + property);
            }
            result = result.flatMap(state -> {
                Property<?> parsed = state.getBlock().getStateDefinition().getProperty(propertySplit[0]);
                if (parsed == null) {
                    return DataResult.error(() -> "Unknown property " + propertySplit[0] + " for block " + identifier);
                }
                return parsed.parseValue(JavaOps.INSTANCE, state, propertySplit[1]);
            });
        }

        return result;
    }

    public static String blockStateToString(BlockState state) {
        Identifier identifier = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (state.isSingletonState()) {
            return identifier.toString();
        }
        return identifier + "[" + blockStatePropertiesToString(state) + "]";
    }

    // thank you rainbow
    private static String blockStatePropertiesToString(BlockState state) {
        return PropertyValueList.of(state.getProperties().stream().map(property -> property.value(state)).toArray(Property.Value[]::new)).getKey();
    }

    @FunctionalInterface
    public interface ReflectionOperation<T> {

        T run() throws IllegalAccessException;
    }

    @FunctionalInterface
    public interface FieldGetter {

        Object get(Field field, Object instance) throws IllegalAccessException;
    }

    private MappingsUtil() {}
}
