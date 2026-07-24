package org.geysermc.mappings.generator.mcpl;

import net.minecraft.client.renderer.LevelEventHandler;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.LevelEvent;
import org.geysermc.mappings.generator.MappingsGenerator;
import org.geysermc.mappings.FileType;
import org.geysermc.mappings.util.MappingsUtil;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class LevelEventTypeGenerator extends MappingsGenerator<String> {

    public LevelEventTypeGenerator(PackOutput output) {
        super(output, FileType.MCPL_LEVEL_EVENT_TYPE);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        StringBuilder output = new StringBuilder();
        List<Field> events = MappingsUtil.listPublicConstants(LevelEvent.class).toList();

        LevelEventHandler mockLevelEventHandler = Mockito.mock(LevelEventHandler.class);
        Mockito.doCallRealMethod().when(mockLevelEventHandler).globalLevelEvent(ArgumentMatchers.anyInt(), ArgumentMatchers.any(), ArgumentMatchers.anyInt());

        int currentValue;
        int lastValue = -1;
        for (int i = 0; i < events.size(); i++) {
            Field event = events.get(i);
            currentValue = MappingsUtil.getConstant(LevelEvent.class, event, Field::getInt);
            if (lastValue != -1 && currentValue - lastValue > 10)
                output.append('\n');

            output.append(event.getName()).append('(').append(currentValue).append("),");

            try {
                mockLevelEventHandler.globalLevelEvent(currentValue, null, 0);
            } catch (Exception exception) {
                // When an exception occurs, the method actually does something, so this is a global level event
                output.append(" // Global level event");
            }

            if (i != (events.size() - 1)) {
                output.append("\n");
            } else {
                int index = output.lastIndexOf(",");
                output.replace(index, index + 1, ";");
            }

            lastValue = currentValue;
        }

        return saveFile(cache, output.toString());
    }

    @Override
    public String getName() {
        return "MCPL/Level Event Type Generator";
    }
}
