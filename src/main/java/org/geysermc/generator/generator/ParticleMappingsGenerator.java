package org.geysermc.generator.generator;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import org.cloudburstmc.protocol.bedrock.data.LevelEvent;
import org.cloudburstmc.protocol.bedrock.data.ParticleType;
import org.geysermc.generator.definitions.particle.ParticleMapping;
import org.geysermc.generator.mappings.FileType;
import org.geysermc.generator.resources.BedrockSamples;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class ParticleMappingsGenerator extends MappingsGenerator<Map<String, ParticleMapping>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final BiMap<String, String> SPECIAL_MCPL_RENAMES = ImmutableBiMap.of(
            "TRIAL_SPAWNER_DETECTION", "TRIAL_SPAWNER_DETECTED_PLAYER",
            "TRIAL_SPAWNER_DETECTION_OMINOUS", "TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS"
    );
    private static final String GEYSER_PACK_NAMESPACE = "geyseropt";
    private final CompletableFuture<BedrockSamples> bedrockSamples;

    public ParticleMappingsGenerator(PackOutput output, CompletableFuture<BedrockSamples> bedrockSamples) {
        super(output, FileType.PARTICLE_MAPPINGS);
        this.bedrockSamples = bedrockSamples;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return bedrockSamples.thenCompose(samples -> samples.with(opened -> {
            List<Identifier> validParticleIds = new ArrayList<>();
            Path particlesPath = opened.getPath("resource_pack/particles");
            try(DirectoryStream<Path> particles = opened.provider().newDirectoryStream(particlesPath, _ -> true)) {
                particles.forEach(path -> {
                    try {
                        JsonElement json = JsonParser.parseString(Files.readString(path));
                        validParticleIds.add(Identifier.parse(json.getAsJsonObject()
                                .getAsJsonObject("particle_effect")
                                .getAsJsonObject("description")
                                .get("identifier").getAsString()));
                    } catch (IOException exception) {
                        throw new RuntimeException("Failed to read particle in bedrock-samples", exception);
                    }
                });
            }
            return validParticleIds;
        })).thenCombine(readExistingFile(), Pair::of).thenCompose(validIdsAndMappings -> {
            Map<String, ParticleMapping> mappings = new Object2ObjectOpenHashMap<>(validIdsAndMappings.getSecond());
            List<Identifier> validParticleIds = validIdsAndMappings.getFirst();

            // First, check for any removed particles that are in our mappings at the moment
            for (String mappedJavaParticle : mappings.keySet()) {
                Identifier javaIdentifier = mcplToIdentifier(mappedJavaParticle);
                if (!BuiltInRegistries.PARTICLE_TYPE.containsKey(javaIdentifier)) {
                    LOGGER.warn("Particle of type {} does not exist anymore, but still is present in the mappings! It will be removed.", javaIdentifier);
                }
            }

            for (Identifier javaParticle : BuiltInRegistries.PARTICLE_TYPE.keySet()) {
                String mcplName = identifierToMcpl(javaParticle);
                ParticleMapping mapping = mappings.computeIfAbsent(mcplName, _ -> ParticleMapping.EMPTY);

                Optional<Identifier> bedrockId = mapping.bedrockId();
                Optional<String> eventType = mapping.cloudburstEventType();

                // Ignore Geyser namespaces as these won't be found in the Bedrock resource pack
                if (bedrockId.isPresent() && !bedrockId.get().getNamespace().equals(GEYSER_PACK_NAMESPACE)) {
                    if (!validParticleIds.contains(bedrockId.get())) {
                        LOGGER.warn("Bedrock particle ID {} for Java particle {} not found in resource pack.", bedrockId.get(), mcplName);
                        bedrockId = Optional.empty();
                    }
                }

                if (eventType.isEmpty() && isBedrockParticleType(mcplName)) {
                    eventType = Optional.of(mcplName); // parity
                } else if (eventType.isPresent() && !isBedrockParticleType(eventType.get())) {
                    LOGGER.warn("Particle type {} for Java particle {} does not exist in the Cloudburst Protocol!", eventType.get(), mcplName);
                    eventType = Optional.empty();
                }

                if (bedrockId.isEmpty() && eventType.isEmpty()) {
                    LOGGER.warn("No Bedrock particle mapped for {}", mcplName);
                    if (validParticleIds.contains(javaParticle)) {
                        LOGGER.warn("But the Bedrock resource pack contains a particle with the ID {}", javaParticle);
                    }
                }

                mappings.put(mcplName, new ParticleMapping(bedrockId, eventType));
            }

            return saveFile(cache, mappings);
        });
    }

    private static Identifier mcplToIdentifier(String mcpl) {
        String specialReverted = SPECIAL_MCPL_RENAMES.inverse().getOrDefault(mcpl, mcpl);
        return Identifier.fromNamespaceAndPath("minecraft", specialReverted.toLowerCase(Locale.ROOT));
    }

    private static String identifierToMcpl(Identifier identifier) {
        String pathUppercase = identifier.getPath().toUpperCase(Locale.ROOT);
        return SPECIAL_MCPL_RENAMES.getOrDefault(pathUppercase, pathUppercase);
    }

    private boolean isBedrockParticleType(String enumName) {
        try {
            // Check if we have a particle type mapping
            ParticleType.valueOf(enumName);
        } catch (IllegalArgumentException _) {
            // No particle type; try level event
            try {
                LevelEvent.valueOf(enumName);
            } catch (IllegalArgumentException _) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "Particle Mappings Generator";
    }
}
