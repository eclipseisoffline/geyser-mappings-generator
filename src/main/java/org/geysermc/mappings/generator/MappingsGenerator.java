package org.geysermc.mappings.generator;

import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.geysermc.mappings.FileType;
import org.geysermc.mappings.FileSystemAccess;
import org.geysermc.mappings.MappingsGenerators;
import org.geysermc.mappings.resources.BedrockSamples;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/// {@link MappingsGenerator} is the abstract base class for all generators (also known as {@link DataProvider}s or simply "providers"). A {@link MappingsGenerator} must
/// export a single {@link FileType}.
///
/// This class is not much more than a simple implementation of {@link FileSystemAccess} in the data-generator context, however,
/// a few helper methods (noted below) are provided to read and write to the {@link FileType}. Implementations are required to implement
/// {@link DataProvider#run(CachedOutput)} and {@link DataProvider#getName()}.
///
/// Sometimes, a {@link MappingsGenerator} will require access to registries, for data-driven content or tags. Access to these is done through the {@link RegistryAccess} interface.
/// This interface is built asynchronously, as such, the generator should have a field holding a `CompletableFuture<RegistryAccess>`. This field can simply be added as constructor parameter,
/// as it can be provided when registering the generator in {@link MappingsGenerators}.
///
/// Then, when running the generator, the generator can use {@link CompletableFuture#thenCompose(Function)} to depend on the {@link RegistryAccess}. As an example:
///
/// ```java
/// public final class MyMappingsGenerator extends MappingsGenerator<SomeType> {
///     private final CompletableFuture<RegistryAccess> registries;
///
///     public MyMappingsGenerator(PackOutput output, CompletableFuture<RegistryAccess> registries) {
///         super(output, FileType.SOME_TYPE);
///         this.registries = registries;
///     }
///
///     @Override
///     public CompletableFuture<?> run(CachedOutput cache) {
///         return registries.thenCompose(registries -> {
///             // Do some stuff with data-driven registries
///             return saveFile(cache, computedFile);
///         });
///     }
/// }
/// ```
///
/// In a similar fashion, {@link BedrockSamples} can also be used, which are loaded asynchronously by {@link MappingsGenerators}.
///
/// @see MappingsGenerators
/// @see MappingsGenerator#saveFile(CachedOutput, Object)
/// @see MappingsGenerator#saveFile(CachedOutput, RegistryAccess, Object)
/// @see MappingsGenerator#readExistingFile()
/// @see MappingsGenerator#readExistingFile(RegistryAccess)
public abstract class MappingsGenerator<T> implements DataProvider, FileSystemAccess {
    private final PackOutput output;
    private final FileType<T> type;

    public MappingsGenerator(PackOutput output, FileType<T> type) {
        this.output = output;
        this.type = type;
    }

    protected CompletableFuture<?> saveFile(CachedOutput cache, T value) {
        return saveFile(cache, type, value);
    }

    protected CompletableFuture<?> saveFile(CachedOutput cache, RegistryAccess registries, T value) {
        return saveFile(cache, registries, type, value);
    }

    protected CompletableFuture<Optional<T>> readExistingFile() {
        return readFile(type);
    }

    protected CompletableFuture<Optional<T>> readExistingFile(RegistryAccess registries) {
        return readFile(type, registries);
    }

    @Override
    public final Path root() {
        return output.getOutputFolder();
    }
}
