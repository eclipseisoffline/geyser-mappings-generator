package org.geysermc.generator.generator.javaclass;

import net.minecraft.core.Direction;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.geysermc.generator.definitions.block.BlockProperties;
import org.geysermc.generator.generator.MappingsGenerator;
import org.geysermc.generator.mappings.FileType;
import org.geysermc.generator.util.MappingsUtil;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;

public final class BlockStatePropertiesGenerator extends MappingsGenerator<String> {

    public BlockStatePropertiesGenerator(PackOutput output) {
        super(output, FileType.BLOCK_STATE_PROPERTIES);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        StringBuilder clazz = new StringBuilder();

        MappingsUtil.listPublicConstants(BlockStateProperties.class)
                .filter(field -> Property.class.isAssignableFrom(field.getType()))
                .forEach(field -> {
                    Property<?> property = MappingsUtil.getConstant(BlockStateProperties.class, field);
                    String className = field.getType().getSimpleName();
                    String parameters = "";
                    String type = className;

                    switch (className) {
                        case "IntegerProperty" -> {
                            // Replicating the IntegerProperty constructor
                            var values = ((IntegerProperty) property).getPossibleValues();
                            int low = values.stream().min(Comparator.naturalOrder()).orElseThrow();
                            int high = values.stream().max(Comparator.naturalOrder()).orElseThrow();
                            parameters = ", " + low + ", " + high;
                        }
                        case "EnumProperty" -> {
                            if (property.getValueClass().equals(Direction.class)) {
                                parameters = ", " + String.join(", ", BlockProperties.allDirections((EnumProperty<Direction>) property));
                                type = "EnumProperty<Direction>";
                            } else if (BlockProperties.geyserHasEnum(property.getValueClass())) {
                                parameters = ", " + BlockProperties.allEnums((EnumProperty<?>) property);
                                type = "EnumProperty<" + property.getValueClass().getSimpleName() + ">";
                            } else {
                                className = "BasicEnumProperty";
                                parameters = ", " + String.join(", ", BlockProperties.allEnumsAsStrings((EnumProperty<?>) property));
                                type = "BasicEnumProperty";
                            }
                        }
                    }

                    clazz.append("public static final ").append(type).append(" ").append(field.getName()).append(" = ").append(className).append(".create(").append(MappingsUtil.wrapInQuotes(property.getName())).append(parameters).append(");\n");
                });

        return saveFile(cache, clazz.toString());
    }

    @Override
    public String getName() {
        return "Block State Properties Generator";
    }
}
