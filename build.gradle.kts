import org.apache.commons.lang3.SystemUtils

plugins {
    idea
    java
    alias(libs.plugins.loom)
    alias(libs.plugins.loom.pack200)
    alias(libs.plugins.shadow)
}

group = properties("modGroup").get()
version = properties("modVersion").get()

// JVM Toolchains:

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

// Minecraft configuration:
loom {
    log4jConfigs.from(file("log4j2.xml"))
    launchConfigs {
        "client" {
            // If you don't want mixins, remove these lines
            property("mixin.debug", "true")
            arg("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
            // End of mixin launch configuration
        }
    }
    runConfigs {
        "client" {
            if (SystemUtils.IS_OS_MAC_OSX) {
                // This argument causes a crash on macOS
                vmArgs.remove("-XstartOnFirstThread")
            }
        }
        remove(getByName("server"))
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        // If you don't want mixins, remove this lines
        mixinConfig("mixins.${properties("modID").get()}.json")
    }
    // If you don't want mixins, remove these lines
    mixin {
        defaultRefmapName.set("mixins.${properties("modID").get()}.refmap.json")
    }
    // End of mixin loom configuration
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

// Dependencies:

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
    // If you don't want to log in with your real minecraft account, remove this line
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft(libs.minecraft)
    mappings(libs.mappings)
    forge(libs.forge)

    // If you don't want mixins, remove these lines
    shadowImpl(libs.mixin.withVersion("0.7.11-SNAPSHOT")) {
        isTransitive = false
    }
    annotationProcessor(libs.mixin.withVersion("0.8.5-SNAPSHOT"))
    // End of mixin dependencies

    // If you don't want to log in with your real minecraft account, remove the following line
    runtimeOnly(libs.devauth)
}

// Tasks:

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.withType(Jar::class) {
    archiveBaseName.set(properties("modID").get())
    manifest.attributes.run {
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"

        // If you don't want mixins, remove these lines
        this["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
        this["MixinConfigs"] = "mixins.${properties("modID").get()}.json"
        // End of mixin manifest names
    }
}

tasks.processResources {
    inputs.property("modVersion", properties("modVersion").get())
    inputs.property("modMCVersion", properties("modMCVersion").get())
    inputs.property("modID", properties("modID").get())
    inputs.property("modGroup", properties("modGroup").get())
    inputs.property("modName", properties("modName").get())
    inputs.property("modGitHub", properties("modGitHub"))

    filesMatching(listOf("mcmod.info", "mixins.${properties("modID").get()}.json")) {
        expand(inputs.properties)
    }

    rename("(.+_at.cfg)", "META-INF/$1")
}

tasks.wrapper {
    gradleVersion = properties("gradleVersion").get()
}

val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    from(tasks.shadowJar)
    input.set(tasks.shadowJar.get().archiveFile)
}

tasks.jar {
    archiveClassifier.set("without-deps")
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
}

tasks.shadowJar {
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
    archiveClassifier.set("all-dev")
    configurations = listOf(shadowImpl)
    doLast {
        configurations.forEach {
            println("Copying jars into mod: ${it.files}")
        }
    }

    // If you want to include other dependencies and shadow them, you can relocate them in here
    fun relocate(name: String) = relocate(name, "${properties("modGroup")}.deps.$name")
}

tasks.assemble.get().dependsOn(tasks.remapJar)

// Gradle build script utilities

fun properties(key: String) = providers.gradleProperty(key)
fun Provider<MinimalExternalModuleDependency>.withVersion(version: String): Provider<String> {
    return map { "${it.module.group}:${it.module.name}:$version" }
}
