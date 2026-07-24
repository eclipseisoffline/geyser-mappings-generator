package org.geysermc.mappings.generator.mcpl;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.geysermc.mappings.generator.MappingsGenerator;
import org.geysermc.mappings.FileType;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class BuiltinSoundGenerator extends MappingsGenerator<String> {

    public BuiltinSoundGenerator(PackOutput output) {
        super(output, FileType.MCPL_BUILTIN_SOUND);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        StringBuilder finalOutput = new StringBuilder();
        int i = 0;
        for (SoundEvent sound : BuiltInRegistries.SOUND_EVENT) {
            Identifier id = BuiltInRegistries.SOUND_EVENT.getKey(sound);
            assert id != null;
            String enumName = id.getPath().replace(".", "_").toUpperCase(Locale.ROOT);

            finalOutput.append(enumName).append("(\"").append(id.getPath()).append("\")");
            if (i != (BuiltInRegistries.SOUND_EVENT.size() - 1)) {
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
        return "MCPL/Builtin Sound Generator";
    }
}
