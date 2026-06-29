package org.geysermc.generator.generator.mcpl;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.geysermc.generator.generator.MappingsGenerator;
import org.geysermc.generator.mappings.FileType;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class ClientboundBlockEventPacketGenerator extends MappingsGenerator<String> {

    public ClientboundBlockEventPacketGenerator(PackOutput output) {
        super(output, FileType.MCPL_BLOCK_EVENT);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        StringBuilder builder = new StringBuilder();

        builder.append(format(Blocks.NOTE_BLOCK));
        builder.append(format(Blocks.STICKY_PISTON));
        builder.append(format(Blocks.PISTON));
        builder.append(format("MOB_SPAWNER", Blocks.SPAWNER));
        builder.append(format(Blocks.CHEST));
        builder.append(format(Blocks.ENDER_CHEST));
        builder.append(format(Blocks.TRAPPED_CHEST));
        builder.append(format(Blocks.END_GATEWAY));
        builder.append(format("SHULKER_BOX_LOWER", Blocks.SHULKER_BOX));
        builder.append(format("SHULKER_BOX_HIGHER", Blocks.BLACK_SHULKER_BOX));
        builder.append(format(Blocks.BELL));
        builder.append(format("COPPER_CHEST_LOWER", Blocks.COPPER_CHEST));
        builder.append(format("COPPER_CHEST_HIGHER", Blocks.WAXED_OXIDIZED_COPPER_CHEST));
        builder.append(format(Blocks.DECORATED_POT));

        return saveFile(cache, builder.toString());
    }

    private static String format(Block block) {
        return format(BuiltInRegistries.BLOCK.getKey(block).getPath().toUpperCase(Locale.ROOT), block);
    }

    private static String format(String name, Block block) {
        return "private static final int " + name + " = " + BuiltInRegistries.BLOCK.getId(block) + ";\n";
    }

    @Override
    public String getName() {
        return "MCPL/Block Event Blocks Generator";
    }
}
