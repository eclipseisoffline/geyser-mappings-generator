package org.geysermc.mappings.generator.mcpl;

import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import org.geysermc.mappings.definitions.mcpl.NetworkCodec;
import org.geysermc.mappings.generator.MappingsGenerator;
import org.geysermc.mappings.FileType;

import java.util.concurrent.CompletableFuture;

public final class NetworkCodecGenerator extends MappingsGenerator<NetworkCodec> {
    private final CompletableFuture<RegistryAccess> registries;

    public NetworkCodecGenerator(PackOutput output, CompletableFuture<RegistryAccess> registries) {
        super(output, FileType.MCPL_NETWORK_CODEC);
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return registries.thenCompose(registries -> saveFile(cache, registries, NetworkCodec.collect(registries)));
    }

    @Override
    public String getName() {
        return "MCPL/Network Codec Generator";
    }
}
