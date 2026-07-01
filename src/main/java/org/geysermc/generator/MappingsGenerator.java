package org.geysermc.generator;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.PlayerEquipment;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.geysermc.generator.definitions.block.BlockMappings;

import java.io.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MappingsGenerator {

    public void generateInteractionData() {
        ClientLevel mockClientLevel = mock(ClientLevel.class);
        when(mockClientLevel.isClientSide()).thenReturn(true);
        mockClientLevel.random = RandomSource.create(); // Used by cave_vines and doors
        when(mockClientLevel.getRandom()).thenReturn(mockClientLevel.random);

        when(mockClientLevel.getBlockState(any())).thenReturn(Blocks.AIR.defaultBlockState());

        Abilities abilities = new Abilities();
        abilities.mayBuild = true;

        LocalPlayer mockPlayer = mock(LocalPlayer.class);

        // Used by bee_hive
        mockPlayer.level = mockClientLevel;
        mockPlayer.position = Vec3.ZERO;
        when(mockPlayer.getInventory()).thenReturn(new Inventory(mockPlayer, new PlayerEquipment(mockPlayer)));

        AtomicBoolean requiresAbilities = new AtomicBoolean(false);
        when(mockPlayer.getAbilities()).then(invocationOnMock -> {
            requiresAbilities.set(true);
            return abilities;
        });

        when(mockPlayer.getDirection()).thenReturn(Direction.UP); // Used by fence_gates

        AtomicReference<ItemStack> item = new AtomicReference<>(ItemStack.EMPTY);
        AtomicBoolean requiresItem = new AtomicBoolean(false);
        when(mockPlayer.getItemInHand(InteractionHand.MAIN_HAND)).then(invocationOnMock -> {
            requiresItem.set(true);
            return item.get();
        });
        when(mockPlayer.getItemInHand(InteractionHand.OFF_HAND)).thenReturn(ItemStack.EMPTY);

        when(mockClientLevel.enabledFeatures()).thenReturn(FeatureFlags.DEFAULT_FLAGS);

        BlockHitResult blockHitResult = new BlockHitResult(Vec3.ZERO, Direction.DOWN, BlockPos.ZERO, true);

        List<String> alwaysConsume = new ArrayList<>();
        List<String> requiresMayBuild = new ArrayList<>();

        for (BlockState state : getAllStates()) {
            try {
                if (state.getBlock() == Blocks.REDSTONE_WIRE) {
                    continue; // Interactions with Redstone wire depend on the wires around it
                } else if (state.getBlock() == Blocks.BELL) {
                    continue; // Interactions with Bells depend on the HitResult
                } else if (state.getBlock() == Blocks.LIGHT) {
                    continue; // Can't interact with light blocks without holding a light item
                } else if (state.getBlock() == Blocks.CAMPFIRE || state.getBlock() == Blocks.SOUL_CAMPFIRE) {
                    continue; // Interactions with campfires depends on campfire recipes
                } else if (state.getBlock() instanceof FlowerPotBlock) {
                    alwaysConsume.add(BlockMappings.blockStateToString(state)); // Contains checks for item, but will always consume the action
                    continue;
                } else if (state.getBlock() == Blocks.DRAGON_EGG) {
                    alwaysConsume.add(BlockMappings.blockStateToString(state)); // Teleports and will always consume the action
                    continue;
                } else if (state.getBlock() == Blocks.CAKE) {
                    continue; // Depends on the player's hunger level
                } else if (state.getBlock() instanceof SignBlock) {
                    alwaysConsume.add(BlockMappings.blockStateToString(state)); // Contains checks for item, but will always consume the action
                    continue;
                } else if (state.getBlock() instanceof RespawnAnchorBlock) {
                    // depends on the dimension (only works in the nether)
                    continue;
                }

                requiresAbilities.set(false);
                abilities.mayBuild = true;

                requiresItem.set(false);
                item.set(ItemStack.EMPTY);

                if (state.getBlock() instanceof BaseEntityBlock baseEntityBlock) {
                    when(mockClientLevel.getBlockEntity(BlockPos.ZERO)).thenReturn(baseEntityBlock.newBlockEntity(BlockPos.ZERO, state));
                } else {
                    when(mockClientLevel.getBlockEntity(BlockPos.ZERO)).thenReturn(null);
                }
                when(mockClientLevel.getBlockState(new BlockPos(0, 0, 0))).thenReturn(state);

                InteractionResult result = state.useWithoutItem(mockClientLevel, mockPlayer, blockHitResult);
                if (!requiresItem.get()) {
                    if (result.consumesAction() && requiresAbilities.get()) {
                        abilities.mayBuild = false;
                        InteractionResult result2 = state.useWithoutItem(mockClientLevel, mockPlayer, blockHitResult);
                        if (result != result2) {
                            requiresMayBuild.add(BlockMappings.blockStateToString(state));
                        }
                    } else if (result.consumesAction()) {
                        alwaysConsume.add(BlockMappings.blockStateToString(state));
                    }
                }
            } catch (Throwable e) {
                // Ignore; this means the block has extended behavior we have to implement manually
                System.out.println("Failed to test interactions for " + BlockMappings.blockStateToString(state) + " due to");
                e.printStackTrace(System.out);
            }
        }

        File mappings = new File("mappings/interactions.json");
        if (!mappings.exists()) {
            System.out.println("Could not find mappings submodule! Did you clone them?");
            return;
        }
        try {
            GsonBuilder builder = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping();
            JsonWriter writer = new JsonWriter(new FileWriter(mappings));
            writer.setIndent("\t");
            builder.create().toJson(new InteractionData(alwaysConsume, requiresMayBuild), InteractionData.class, writer);
            writer.close();
            System.out.println("Finished interaction writing process!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<BlockState> getAllStates() {
        List<BlockState> states = new ArrayList<>();
        BuiltInRegistries.BLOCK.forEach(block -> states.addAll(block.getStateDefinition().getPossibleStates()));
        return states.stream().sorted(Comparator.comparingInt(Block::getId)).collect(Collectors.toList());
    }
}
