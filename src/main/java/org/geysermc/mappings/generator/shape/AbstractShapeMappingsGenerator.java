package org.geysermc.mappings.generator.shape;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.world.phys.AABB;
import org.geysermc.mappings.FileType;
import org.geysermc.mappings.definitions.shape.BlockShapeMappings;
import org.geysermc.mappings.generator.MappingsGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractShapeMappingsGenerator extends MappingsGenerator<BlockShapeMappings> {

    public AbstractShapeMappingsGenerator(PackOutput output, FileType<BlockShapeMappings> type) {
        super(output, type);
    }

    protected abstract List<List<AABB>> collectShapes();

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<List<AABB>> shapes = collectShapes();
        List<List<List<Double>>> rawShapes = new ArrayList<>();

        IntList indices = new IntArrayList(shapes.size());
        for (List<AABB> shapesForThisInstance : shapes) {
            List<List<Double>> rawShapesForThisInstance = new ArrayList<>();
            shapesForThisInstance.forEach(item -> {
                List<Double> coordinateList = Lists.newArrayList();
                // Convert AABB class to an array of coordinates
                // They need to be converted from min/max coordinates to centres and sizes
                coordinateList.add(item.minX + ((item.maxX - item.minX) / 2));
                coordinateList.add(item.minY + ((item.maxY - item.minY) / 2));
                coordinateList.add(item.minZ + ((item.maxZ - item.minZ) / 2));

                coordinateList.add(item.maxX - item.minX);
                coordinateList.add(item.maxY - item.minY);
                coordinateList.add(item.maxZ - item.minZ);

                rawShapesForThisInstance.add(coordinateList);
            });
            if (!rawShapes.contains(rawShapesForThisInstance)) {
                rawShapes.add(rawShapesForThisInstance);
            }
            indices.add(rawShapes.lastIndexOf(rawShapesForThisInstance));
        }

        return saveFile(cache, new BlockShapeMappings(indices, rawShapes));
    }
}
