import net.fabricmc.loom.task.DownloadTask
import java.nio.file.Files
import java.nio.file.FileSystems
import java.nio.file.StandardCopyOption

val targetJavaVersion = 25

val resourcePackPath = file("bedrockresourcepack.zip")
val bedrockSamples = file("bedrock-samples.zip")

group = "org.geysermc.mappings-generator"
version = "1.1.0"

plugins {
    alias(libs.plugins.fabric.loom)
}

repositories {
    mavenCentral()

    maven("https://maven.fabricmc.net") {
        name = "Fabric"
    }

    maven("https://repo.opencollab.dev/main") {
        name = "OpenCollab"
    }
}

dependencies {
    minecraft(libs.minecraft)

    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)

    implementation(libs.lombok)
    annotationProcessor(libs.lombok)

    implementation(libs.commons.text)
    implementation(libs.mockito.core)
    implementation(libs.bundles.cloudburst.protocol)
}

java {
    val version = JavaVersion.toVersion(targetJavaVersion)

    toolchain {
        languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }

    sourceCompatibility = version
    targetCompatibility = version
}

loom {
    accessWidenerPath = file("src/main/resources/mappings-generator.accesswidener")
}

tasks {
    val samplesTask = register<DownloadTask>("downloadBedrockSamples") {
        url = "https://github.com/Mojang/bedrock-samples/archive/refs/tags/v${libs.versions.minecraft.bedrock.get()}.zip"
        output = bedrockSamples
    }
    val resourcePackTask = register<CreateResourcePackTask>("resourcePack") {
        dependsOn(samplesTask)
        bedrockSamples = samplesTask.get().output
        packFile = resourcePackPath
    }

    val blockPaletteTask = register<DownloadTask>("downloadBlockPalette") {
        url = "https://raw.githubusercontent.com/CloudburstMC/Data/master/block_palette.nbt"
        output = file("palettes/block_palette.nbt")
    }

    val runtimeItemStatesTask = register<DownloadTask>("downloadRuntimeItemStates") {
        url = "https://raw.githubusercontent.com/CloudburstMC/Data/master/runtime_item_states.json"
        output = file("palettes/runtime_item_states.json")
    }

    val itemComponentsTask = register<DownloadTask>("downloadItemComponents") {
        url = "https://raw.githubusercontent.com/CloudburstMC/Data/master/item_components.nbt"
        output = file("palettes/item_components.nbt")
    }

    val downloadAll = register("downloadAll") {
        dependsOn(resourcePackTask, blockPaletteTask, runtimeItemStatesTask, itemComponentsTask)
    }

    withType<JavaCompile>().configureEach {
        options.release = targetJavaVersion
    }

    processResources {
        inputs.property("version", version)
        inputs.property("minecraft_version", libs.versions.minecraft.java.get())
        inputs.property("loader_version", libs.versions.fabric.loader.get())
        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(
                mapOf(
                    "version" to version,
                    "minecraft_version" to libs.versions.minecraft.java.get(),
                    "loader_version" to libs.versions.fabric.loader.get()
                )
            )
        }
    }
}

abstract class CreateResourcePackTask : DefaultTask() {

    @get:InputFile
    abstract val bedrockSamples: RegularFileProperty

    @get:OutputFile
    abstract val packFile: RegularFileProperty

    @TaskAction
    fun greet() {
        val samples = bedrockSamples.get().asFile.toPath()
        val output = packFile.get().asFile.toPath()
        Files.copy(samples, output, StandardCopyOption.REPLACE_EXISTING)

        FileSystems.newFileSystem(output)
            .use { fileSystem ->
                val root = fileSystem.rootDirectories.first()!!

                // the root just has one folder, eg "bedrock-samples-1.19.80.2"
                val subFolder = Files.walk(root, 1)
                    .filter { e -> e.toString().contains("bedrock-samples") }
                    .findFirst().get()

                val pack = subFolder.resolve("resource_pack")

                // move the resource pack contents to the root
                Files.walk(pack).use { stream ->
                    stream.filter { e -> e != pack }
                        .forEach { e ->
                            // order is important here so that empty destination directories are created first
                            Files.move(e, root.resolve(pack.relativize(e)))
                        }
                }

                // delete everything in the old folder, including itself
                Files.walk(subFolder).use { stream ->
                    stream.sorted(Comparator.reverseOrder()) // delete files before their parent directories
                        .forEach(Files::delete)
                }
            }
    }
}
