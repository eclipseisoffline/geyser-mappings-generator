package org.geysermc.mappings.definitions.block;

import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static org.geysermc.mappings.util.MappingsUtil.wrapInQuotes;

public final class BlockProperties {
    private static final Set<Class<? extends Enum<?>>> GEYSER_ENUMS = Set.of(ChestType.class, Direction.Axis.class,
            FrontAndTop.class);

    private BlockProperties() {}

    public static boolean geyserHasEnum(Class<?> clazz) {
        return GEYSER_ENUMS.contains(clazz);
    }

    public static List<String> allDirections(EnumProperty<Direction> property) {
        return property.getPossibleValues().stream().map(direction -> "Direction." + direction.name()).toList();
    }

    public static String allEnums(EnumProperty<?> enumProperty) {
        Collection<? extends Enum<?>> possibleValues = enumProperty.getPossibleValues();
        Enum<?>[] allValues = enumProperty.getValueClass().getEnumConstants();
        Stream<Enum<?>> stream = Arrays.stream(allValues).filter(anEnum -> !possibleValues.contains(anEnum));
        String result;
        if (stream.findAny().isPresent()) {
            // Only some values are present
            result = String.join(", ", possibleValues.stream().map(value -> enumProperty.getValueClass().getSimpleName() + "." + value.name()).toList());
        } else {
            // All values are used
            result = enumProperty.getValueClass().getSimpleName() + ".VALUES";
        }
        return result;
    }

    public static List<String> allEnumsAsStrings(EnumProperty<?> enumProperty) {
        return enumProperty.getPossibleValues().stream().map(object -> wrapInQuotes(object.toString().toLowerCase(Locale.ROOT))).toList();
    }
}
