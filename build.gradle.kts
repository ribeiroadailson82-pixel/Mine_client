plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
}

group = "com.mineclient"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

loom {
    launchConfigs {
        named("client") {
        }
    }
    runConfigs {
        named("client") {
            isIdeConfigGenerated = true
        }
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
    }
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    // compila bytecode Java 8 (exigido pelo MC 1.8.9) com qualquer JDK 9+
    options.release.set(8)
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("mcmod.info") {
        expand("version" to project.version, "mcversion" to "1.8.9")
    }
}
