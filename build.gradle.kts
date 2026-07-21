group = "org.geysermc.mappings-generator"
version = "1.1.0"

val targetJavaVersion = 25

// Have to do this to explicitly attach the Mockito Java agent: https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#0.3
val mockitoAgent = configurations.create("mockitoAgent")

plugins {
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.mod.publish.plugin)
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

    implementation(libs.commons.text)
    implementation(libs.mockito.core)
    implementation(libs.bundles.cloudburst.protocol)

    mockitoAgent(libs.mockito.core) {
        isTransitive = false
    }
}

java {
    val version = JavaVersion.toVersion(targetJavaVersion)

    toolchain {
        languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }

    sourceCompatibility = version
    targetCompatibility = version
}

fabricApi {
    configureDataGeneration {
        outputDirectory = file("mappings")
        addToResources = false
        client = true
    }
}

loom {
    runConfigs {
        named("datagen") {
            jvmArguments.add("-Dline.separator=\u000a")
            jvmArguments.add("-javaagent:${mockitoAgent.asPath}")
        }

        register("MCPL") {
            inherit(getByName("datagen"))
            jvmArguments.add("-Dgeyser.providers.selected=mcpl")
        }

        register("javaclass") {
            inherit(getByName("datagen"))
            jvmArguments.add("-Dgeyser.providers.selected=javaclass")
        }

        register("mappings") {
            inherit(getByName("datagen"))
            jvmArguments.add("-Dgeyser.providers.selected=mappings")
        }
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = targetJavaVersion
    }

    getByName("runClient") {
        enabled = false
    }

    getByName("runClientRenderDoc") {
        enabled = false
    }

    getByName("runServer") {
        enabled = false
    }

    processResources {
        inputs.property("version", version)
        inputs.property("minecraft_version", libs.versions.minecraft.java.get())
        inputs.property("loader_version", libs.versions.fabric.loader.get())
        inputs.property("bedrock_version", libs.versions.minecraft.bedrock.tag.get())
        inputs.property("bedrock_data_sha", libs.versions.minecraft.bedrock.data.get())
        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(
                mapOf(
                    "version" to version,
                    "minecraft_version" to libs.versions.minecraft.java.get(),
                    "loader_version" to libs.versions.fabric.loader.get(),
                    "bedrock_version" to libs.versions.minecraft.bedrock.tag.get(),
                    "bedrock_data_sha" to libs.versions.minecraft.bedrock.data.get()
                )
            )
        }
    }

    val prepareFullRelease = register<Zip>("prepareFullRelease") {
        description = "Zips the contents of the \"mappings\" folder to build/mappings"

        dependsOn("runDatagen")

        from("mappings")
        exclude(".cache")
        // Exclude bedrock-data.zip and bedrock-samples.zip
        exclude("*.zip")

        destinationDirectory = project.layout.buildDirectory.dir("mappings")

        archiveBaseName = "mappings"
        // Maybe put Java/bedrock version in the appendix?
        archiveVersion = "v${version}"
        archiveClassifier = "full"
    }

    val prepareMinRelease = register<Zip>("prepareMinRelease") {
        description = "Zips the contents of the \"mappings/mappings\" folder to build/mappings"

        dependsOn("runDatagen")

        from("mappings/mappings")

        destinationDirectory = project.layout.buildDirectory.dir("mappings")

        archiveBaseName = "mappings"
        // Maybe put Java/bedrock version in the appendix?
        archiveVersion = "v${version}"
        archiveClassifier = "min"
    }

    val prepareRelease = register("prepareRelease") {
        description = "Creates the ZIP files to be published for release"

        dependsOn(prepareFullRelease)
        dependsOn(prepareMinRelease)
    }
}

// This is intended for publishing mod JARs to Curseforge/Modrinth/GH releases, but it works just fine for publishing anything else to GH releases!
publishMods {
    changelog = "TODO"
    type = STABLE // TODO check commitish

    file.set(tasks.named<Zip>("prepareFullRelease").flatMap { it.archiveFile })
    additionalFiles.from(tasks.named<Zip>("prepareMinRelease").flatMap { it.archiveFile })

    val githubToken = providers.environmentVariable("GITHUB_TOKEN")

    dryRun = !githubToken.isPresent

    github {
        accessToken = githubToken
        repository = providers.environmentVariable("GITHUB_REPOSITORY")
        commitish = providers.environmentVariable("GITHUB_REF_NAME")
    }
}
