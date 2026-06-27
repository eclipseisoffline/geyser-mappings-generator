package org.geysermc.generator;

public class Main {

    public static void main(String[] args) {
        Util.initialize();

        MappingsGenerator generator = new MappingsGenerator();
        BlockGenerator.generate();
        CollisionGenerator.generate();
        generator.generateSounds();
        generator.generateMapColors();
        generator.generateParticles();
        generator.generateInteractionData();
        RecipeGenerator.generate();
        UtilGenerator.generate();
    }
}
