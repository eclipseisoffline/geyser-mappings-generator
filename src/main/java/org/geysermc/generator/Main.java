package org.geysermc.generator;

public class Main {

    public static void main(String[] args) {
        Util.initialize();

        MappingsGenerator generator = new MappingsGenerator();
        BlockGenerator.generate();
        generator.generateSounds();
        generator.generateInteractionData();
        RecipeGenerator.generate();
        UtilGenerator.generate();
    }
}
