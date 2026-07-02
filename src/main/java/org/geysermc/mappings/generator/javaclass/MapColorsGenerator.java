package org.geysermc.mappings.generator.javaclass;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.material.MapColor;
import org.geysermc.mappings.generator.MappingsGenerator;
import org.geysermc.mappings.FileType;
import org.geysermc.mappings.mixin.MapColorAccessor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class MapColorsGenerator extends MappingsGenerator<String> {

    public MapColorsGenerator(PackOutput output) {
        super(output, FileType.MAP_COLOR);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<Color> mapColors = new ArrayList<>();
        for (MapColor color : MapColorAccessor.getMaterialColors()) {
            if (color == null) {
                continue;
            }

            for (MapColor.Brightness brightness : MapColor.Brightness.values()) {
                int rgb = color.calculateARGBColor(brightness);
                mapColors.add(new Color(rgb, true));
            }
        }

        StringBuilder finalOutput = new StringBuilder();
        for (int i = 0; i < mapColors.size(); i++) {
            Color color = mapColors.get(i);
            finalOutput.append("COLOR_").append(i).append("(").append(color.getRed()).append(", ").append(color.getGreen()).append(", ").append(color.getBlue()).append("),\n");
        }

        // Remap the empty colors
        finalOutput = new StringBuilder(finalOutput.toString().replaceAll("\\(0, 0, 0\\)", "(-1, -1, -1)"));

        // Fix the end
        finalOutput = new StringBuilder(finalOutput.substring(0, finalOutput.length() - 2) + ";");

        return saveFile(cache, finalOutput.toString());
    }

    @Override
    public String getName() {
        return "MapColor generator";
    }
}
