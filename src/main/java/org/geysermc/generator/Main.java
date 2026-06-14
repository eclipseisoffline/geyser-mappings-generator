package org.geysermc.generator;

public class Main {

    static void main() {
        Util.initialize();

        MappingsGenerator generator = new MappingsGenerator();
        generator.generateItems();
        // TODO: why does this break when blocks or interactions are generated first?
        DataComponentGenerator.generate();
        BlockGenerator.generate();
        CollisionGenerator.generate();
        BlockShapeGenerator.generate();
        generator.generateSounds();
        generator.generateBiomes();
        generator.generateMapColors();
        generator.generateParticles();
        generator.generateInteractionData();
        RecipeGenerator.generate();
        UtilGenerator.generate();
    }
}
