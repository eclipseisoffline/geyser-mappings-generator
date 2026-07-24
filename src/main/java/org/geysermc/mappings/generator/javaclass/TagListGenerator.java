package org.geysermc.mappings.generator.javaclass;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DialogTags;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import org.geysermc.mappings.generator.MappingsGenerator;
import org.geysermc.mappings.FileType;
import org.geysermc.mappings.util.MappingsUtil;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public final class TagListGenerator extends MappingsGenerator<String> {
    private final String geyserType;
    private final Class<?> tagDefinitions;

    public TagListGenerator(PackOutput output, FileType<String> fileType, String geyserType, Class<?> tagDefinitions) {
        super(output, fileType);
        this.geyserType = geyserType;
        this.tagDefinitions = tagDefinitions;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        StringBuilder tags = new StringBuilder();
        MappingsUtil.listPublicConstants(tagDefinitions)
                .forEach(field -> {
                    TagKey<?> key = MappingsUtil.getConstant(tagDefinitions, field);
                    String path = key.location().getPath();
                    String fieldName = path.replace("/", "_").toUpperCase(Locale.ROOT);
                    tags.append("public static final Tag<").append(geyserType).append("> ").append(fieldName).append(" = create(").append(MappingsUtil.wrapInQuotes(path)).append(");\n");
                });
        return saveFile(cache, tags.toString());
    }

    @Override
    public String getName() {
        return geyserType + " Tag Generator";
    }

    private static Function<PackOutput, DataProvider> of(FileType<String> fileType, String geyserType, Class<?> tagDefinitions) {
        return output -> new TagListGenerator(output, fileType, geyserType, tagDefinitions);
    }

    public static void addProviders(Consumer<Function<PackOutput, DataProvider>> factoryConsumer) {
        factoryConsumer.accept(of(FileType.BLOCK_TAGS, "Block", BlockTags.class));
        factoryConsumer.accept(of(FileType.DIALOG_TAGS, "Dialog", DialogTags.class));
        factoryConsumer.accept(of(FileType.ITEM_TAGS, "Item", ItemTags.class));
        factoryConsumer.accept(of(FileType.ENCHANTMENT_TAGS, "Enchantment", EnchantmentTags.class));
    }
}
