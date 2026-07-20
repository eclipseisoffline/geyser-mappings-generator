package org.geysermc.mappings.generator.mcpl;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import org.geysermc.mappings.generator.MappingsGenerator;
import org.geysermc.mappings.FileType;
import org.geysermc.mappings.mixin.accessor.StatAccessor;
import org.geysermc.mappings.util.MappingsUtil;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class CustomStatisticGenerator extends MappingsGenerator<String> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public CustomStatisticGenerator(PackOutput output) {
        super(output, FileType.MCPL_CUSTOM_STATISTIC);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        StringBuilder finalOutput = new StringBuilder();
        int i = 0;
        for (Identifier id : BuiltInRegistries.CUSTOM_STAT) {
            Stat<?> stat = Stats.CUSTOM.get(id);
            StatFormatter formatter = ((StatAccessor) stat).getFormatter();

            String format = "INTEGER";
            if (formatter == StatFormatter.DIVIDE_BY_TEN) {
                format = "TENTHS";
            } else if (formatter == StatFormatter.DISTANCE) {
                format = "DISTANCE";
            } else if (formatter == StatFormatter.TIME) {
                format = "TIME";
            } else if (formatter != StatFormatter.DEFAULT) {
                MappingsUtil.findConstantNameInClass(StatFormatter.class, formatter)
                        .ifPresentOrElse(name -> LOGGER.error("Unknown StatFormatter {} for stat {}, it might be called {}", formatter, id, name),
                                () -> LOGGER.error("Unknown StatFormatter {} for stat {}", formatter, id));
            }

            finalOutput.append(id.getPath().toUpperCase(Locale.ROOT));
            if (!format.equals("INTEGER")) {
                finalOutput.append("(StatisticFormat.").append(format).append(")");
            }

            if (i != (BuiltInRegistries.CUSTOM_STAT.size() - 1)) {
                finalOutput.append(",\n");
            } else {
                finalOutput.append(";");
            }
            i++;
        }

        return saveFile(cache, finalOutput.toString());
    }

    @Override
    public String getName() {
        return "MCPL/Custom Statistic Generator";
    }
}
