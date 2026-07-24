package org.geysermc.mappings.names.renamers;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.animal.chicken.ChickenSoundVariants;
import org.geysermc.mappings.names.InstanceRenamer;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class SoundEventNames {
    public static final InstanceRenamer<SoundEvent, Set<String>, Optional<String>> INSTANCE = InstanceRenamer.ofOptional(SoundEventNames::mapSoundEvent, builder -> builder
            // Separate sound events on Java, combined on Bedrock
            .rename(SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS, "ambient.underwater.additions")
            .rename(SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_RARE, "ambient.underwater.additions")
            .rename(SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE, "ambient.underwater.additions")

            // Sound events that delegate to other events on Java
            .rename(SoundEvents.ANVIL_FALL, "hit.anvil")
            .rename(SoundEvents.ANVIL_HIT, "hit.anvil")

            .rename(SoundEvents.CHORUS_FRUIT_TELEPORT, "mob.endermen.portal")
            .rename(SoundEvents.COBWEB_PLACE, "break.web")

            .rename(SoundEvents.GLOW_ITEM_FRAME_ADD_ITEM, "block.itemframe.add_item")
            .rename(SoundEvents.GLOW_ITEM_FRAME_BREAK, "block.itemframe.break")
            .rename(SoundEvents.GLOW_ITEM_FRAME_PLACE, "block.itemframe.place")
            .rename(SoundEvents.GLOW_ITEM_FRAME_REMOVE_ITEM, "block.itemframe.remove_item")
            .rename(SoundEvents.GLOW_ITEM_FRAME_ROTATE_ITEM, "block.itemframe.rotate_item")

            .rename(SoundEvents.SALMON_DEATH, "mob.fish.hurt")
            .rename(SoundEvents.SHELF_PLACE, "break.chiseled_bookshelf")
            .rename(SoundEvents.SULFUR_SPIKE_PLACE, "block.sulfur.place")
            .rename(SoundEvents.POLISHED_TUFF_PLACE, "place.tuff")

            // Simple renames
            .rename(SoundEvents.TRIAL_SPAWNER_PLACE, "trial_spawner.place")
            .rename(SoundEvents.HOE_TILL, "use.gravel") // Yes, actually. The subtitle matches on Bedrock
            .rename(SoundEvents.SHOVEL_FLATTEN, "use.grass") // Yep.
            .rename(SoundEvents.ILLUSIONER_CAST_SPELL, "mob.evocation_illager.cast_spell")

            .rename(SoundEvents.ITEM_FRAME_ADD_ITEM, "block.itemframe.add_item")
            .rename(SoundEvents.ITEM_FRAME_BREAK, "block.itemframe.break")
            .rename(SoundEvents.ITEM_FRAME_PLACE, "block.itemframe.place")
            .rename(SoundEvents.ITEM_FRAME_REMOVE_ITEM, "block.itemframe.remove_item")
            .rename(SoundEvents.ITEM_FRAME_ROTATE_ITEM, "block.itemframe.rotate_item")

            .rename(SoundEvents.NETHER_WART_PLANTED, "place.nether_wart")

            .rename(SoundEvents.PARROT_IMITATE_CREEPER, "imitate.fuse") // Thanks for the consistency, Mojang
            .rename(SoundEvents.PARROT_IMITATE_ENDER_DRAGON, "mob.imitate.enderdragon")
            .rename(SoundEvents.PARROT_IMITATE_EVOKER, "mob.imitate.evocation_illager")
            .rename(SoundEvents.PARROT_IMITATE_ILLUSIONER, "mob.imitate.evocation_illager")
            .rename(SoundEvents.PARROT_IMITATE_MAGMA_CUBE, "mob.imitate.magmacube")

            .rename(SoundEvents.SPAWNER_PLACE, "block.mob_spawner.place")
            .rename(SoundEvents.STRIDER_HAPPY, "mob.strider.tempt")
            .rename(SoundEvents.VAULT_PLACE, "vault.place")

            // Holder SoundEvents below
            .<Holder<SoundEvent>>mapType(Holder::value)
            // Sound events that delegate to other events on Java
            .rename(SoundEvents.CHICKEN_SOUNDS.get(ChickenSoundVariants.SoundSet.CLASSIC).adultSounds().deathSound(), "mob.chicken.hurt")

            // Simple renames
            .rename(SoundEvents.MUSIC_BIOME_SOUL_SAND_VALLEY, "music.game.soulsand_valley"));

    private SoundEventNames() {}

    private static Optional<String> mapSoundEvent(SoundEvent soundEvent, Set<String> bedrockSounds) {
        String identifier = Objects.requireNonNull(BuiltInRegistries.SOUND_EVENT.getKey(soundEvent)).getPath();
        if (bedrockSounds.contains(identifier)) {
            return Optional.of(identifier);
        }

        if (identifier.startsWith("block.note_block")) {
            identifier = "note" + identifier.substring(identifier.lastIndexOf('.'));
        } else if (identifier.startsWith("block.")) {
            identifier = identifier.replace("weeping_vines", "roots");
            identifier = identifier.replace("block.gilded_blackstone.", "block.stone.");
            identifier = identifier.replace("block.metal.", "block.stone.");
            identifier = identifier.replace("block.vine", "block.vines");
            identifier = identifier.replace("small_dripleaf", "big_dripleaf");
            identifier = identifier.replace("rooted_dirt", "dirt_with_roots");
            identifier = identifier.replace("nether_ore", "nether_gold_ore"); // ??? mojang
            identifier = identifier.replace("netherite_block", "netherite");
            identifier = identifier.replace("polished_deepslate", "deepslate");
            identifier = identifier.replace("deepslate_tiles", "deepslate_bricks");
            identifier = identifier.replace("flowering_azalea", "azalea");
            identifier = identifier.replace("frogspawn", "frog_spawn");
            identifier = identifier.replace("moss_carpet", "moss");
            identifier = identifier.replace("nether_bricks", "nether_brick");
            identifier = identifier.replace("wart_block", "nether_wart");

            identifier = identifier.substring("block.".length());
            String[] parts = identifier.split("\\.");
            if (parts.length > 1) {
                identifier = parts[1] + "." + parts[0];
            }
        } else if (identifier.startsWith("item.brush")) {
            String[] parts = identifier.split("\\.");
            identifier = "brush.suspicious_" + parts[3];
        } else if (identifier.startsWith("item.spear.lunge")) {
            // Java: item.spear.lunge_1; Bedrock: item.enchant.lunge1
            char last = identifier.charAt(identifier.length() - 1);
            identifier = "item.enchant.lunge" + last;
        } else if (identifier.startsWith("music.")) {
            // a lot of the bedrock names use "game" instead of overworld or nether
            String[] parts = identifier.split("\\.", 3);
            if (parts.length == 3) {
                identifier = "music.game." + parts[2];
            }
        } else if (identifier.startsWith("entity.")) {
            identifier = identifier.replace("entity.", "mob.");

            if (identifier.contains("donkey")) {
                identifier = identifier.replace("donkey", "horse.donkey");
            } else if (identifier.contains("goat.screaming")) {
                identifier = identifier.replace(".screaming", "");

                String screamer = identifier + ".screamer";
                if (bedrockSounds.contains(screamer)) {
                    identifier = screamer; // specific screamer sound
                }
                // otherwise uses normal goat sound
            } else if (identifier.startsWith("mob.wolf_")) {
                if (identifier.contains("wolf_puglin")) {
                    identifier = identifier.replace("wolf_puglin", "wolf.puglin");
                }

                if (identifier.contains("wolf_sad")) {
                    identifier = identifier.replace("wolf_sad", "wolf.sad");
                }

                if (identifier.contains("wolf_angry")) {
                    identifier = identifier.replace("wolf_angry", "wolf.mad");
                }

                if (identifier.contains("wolf_big")) {
                    identifier = identifier.replace("wolf_big", "wolf.big");
                }

                if (identifier.contains("wolf_cute")) {
                    identifier = identifier.replace("wolf_cute", "wolf.cute");
                }

                if (identifier.contains("wolf_grumpy")) {
                    identifier = identifier.replace("wolf_grumpy", "wolf.grumpy");
                }

                if (identifier.contains(".pant")) {
                    identifier = identifier.replace(".pant", ".panting");
                }

                identifier = identifier.replace("ambient", "bark");
            } else if (identifier.startsWith("mob.parrot.imitate")) {
                identifier = identifier.replace("parrot.", "");
            }
        } else {
            identifier = identifier.replace("item.armor", "armor");
        }

        if (bedrockSounds.contains(identifier)) {
            return Optional.of(identifier);
        }
        return Optional.empty();
    }
}
