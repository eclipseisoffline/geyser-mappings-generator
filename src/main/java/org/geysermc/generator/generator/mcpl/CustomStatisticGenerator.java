package org.geysermc.generator.generator.mcpl;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import org.geysermc.generator.generator.MappingsGenerator;
import org.geysermc.generator.mappings.FileType;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class CustomStatisticGenerator extends MappingsGenerator<String> {

    public CustomStatisticGenerator(PackOutput output) {
        super(output, FileType.MCPL_CUSTOM_STATISTIC);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        StringBuilder finalOutput = new StringBuilder();
        int i = 0;
        for (Identifier id : BuiltInRegistries.CUSTOM_STAT) {
            Stat<?> stat = Stats.CUSTOM.get(id);

            String format;
            if (stat.formatter == StatFormatter.DIVIDE_BY_TEN) {
                format = "TENTHS";
            } else if (stat.formatter == StatFormatter.DISTANCE) {
                format = "DISTANCE";
            } else if (stat.formatter == StatFormatter.TIME) {
                format = "TIME";
            } else {
                format = "INTEGER";
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

        return saveTextFile(cache, finalOutput.toString());
    }

    @Override
    public String getName() {
        return "MCPL/Custom Statistic Generator";
    }
}
