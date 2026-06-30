package org.geysermc.generator.util;

import net.minecraft.nbt.CompoundTag;
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
