package org.geysermc.mappings.generator.javaclass;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import org.geysermc.mappings.generator.MappingsGenerator;
import org.geysermc.mappings.FileType;
import org.geysermc.mappings.util.FieldConstructor;
import org.geysermc.mappings.util.MappingsUtil;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class GameRulesGenerator extends MappingsGenerator<String> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public GameRulesGenerator(PackOutput output) {
        super(output, FileType.GAME_RULES);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        StringBuilder clazz = new StringBuilder();

        for (GameRule<?> gameRule : BuiltInRegistries.GAME_RULE) {
            Identifier key = BuiltInRegistries.GAME_RULE.getKey(gameRule);
            assert key != null;

            Class<?> valueClass = gameRule.valueClass();
            String classType = valueClass.getSimpleName();
            String geyserType;

            if (valueClass == Boolean.class) {
                geyserType = "Bool";
            } else if (valueClass == Integer.class) {
                geyserType = "Int";
            } else {
                LOGGER.error("Skipping game rule {} because its value class ({}) is not supported!", key, classType);
                continue;
            }

            FieldConstructor constructor = new FieldConstructor("GameRule", classType);
            constructor.declareFieldName(gameRule.getIdentifier().getPath().toUpperCase(Locale.ROOT));

            constructor.declareClassName("GameRule." + geyserType);
            constructor.addParameter(MappingsUtil.wrapInQuotes(key.getPath()));
            constructor.addParameter("GameRuleCategory." + gameRule.category().getDescriptionId().getPath().toUpperCase(Locale.ROOT));

            // also add min/max for integer gamerules
            if (gameRule.argument() instanceof IntegerArgumentType integerArgumentType) {
                constructor.addParameter(integerArgumentType.getMinimum());

                int max = integerArgumentType.getMaximum();
                if (max == Integer.MAX_VALUE) {
                    constructor.addParameter("Integer.MAX_VALUE");
                } else {
                    constructor.addParameter(integerArgumentType.getMaximum());
                }
            }

            constructor.addFinishingParameter(gameRule.defaultValue().toString());
            clazz.append(constructor.finish()).append("\n");
        }

        return saveFile(cache, clazz.toString());
    }

    @Override
    public String getName() {
        return "Game Rules Generator";
    }
}
