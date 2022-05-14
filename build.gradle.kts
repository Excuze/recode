plugins {
    id("fabric-loom") version "0.11-SNAPSHOT"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.openjfx.javafxplugin") version "0.0.13"
}

val modVersion: String by project
version = modVersion
val mavenGroup: String by project
group = mavenGroup

base {
    val archivesBaseName: String by project
    archivesName.set(archivesBaseName)
}

repositories {
    jcenter()
    maven {
        name = "CottonMC"
        url = uri("https://server.bbkr.space/artifactory/libs-release")
    }
    maven {
        url = uri("https://maven.shedaniel.me/")
    }
    maven {
        url = uri("https://maven.terraformersmc.com/")
    }
    mavenCentral()
}

val shade by configurations.creating {
    isCanBeResolved = true
    exclude(group = "org.slf4j")
}

dependencies {
    fun shadeImpl(notation: String) {
        implementation(notation)
        shade(notation)
    }

    val minecraftVersion: String by project
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())

    val loaderVersion: String by project
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    val fabricVersion: String by project
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")

    // https://github.com/CottonMC/LibGui/releases
    modImplementation("io.github.cottonmc:LibGui:5.4.0+1.18.2")

	modImplementation("com.terraformersmc:modmenu:3.2.1")
	modImplementation("me.shedaniel.cloth:cloth-config-fabric:6.2.62")

    // discord rpc
    shadeImpl("com.jagrosh:DiscordIPC:0.4")

    // websocket TODO: clean this up
    shadeImpl("org.java-websocket:Java-WebSocket:1.5.3")
    modImplementation("javax.websocket:javax.websocket-api:1.1")
    shadeImpl("io.socket:socket.io-client:2.0.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17

	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }

    jar {
        enabled = false
    }

    shadowJar {
        configurations = listOf(shade)
        destinationDirectory.set(file("build/devlibs"))
        archiveClassifier.set("dev")
    }

    remapJar {
        inputFile.value(shadowJar.get().archiveFile)
    }
}

javafx {
    modules = listOf("javafx.media")
}