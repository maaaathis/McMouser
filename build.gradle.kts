plugins {
    // Applies the correct loom variant based on the active Minecraft version.
    id("dev.kikugie.loom-back-compat")
}

val modId = property("mod.id") as String
val modName = property("mod.name") as String
val modVersion = property("mod.version") as String
val mcCompat: String = sc.properties["mod.mc_compat"]

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}
val mixinJava = "JAVA_${requiredJava.majorVersion}"

// Do NOT set group = ... (managed by loom-back-compat).
version = "$modVersion+${sc.current.version}"
base.archivesName = modId

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    // Applies Mojang mappings, deobfuscating obfuscated (older) versions too.
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
}

loom {
    runConfigs.all {
        runDir = "../../run" // Share the run directory between versions.
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    processResources {
        val props = mapOf(
            "id" to modId,
            "name" to modName,
            "version" to modVersion,
            "minecraft" to mcCompat,
        )
        props.forEach { (key, value) -> inputs.property(key, value) }
        filesMatching("fabric.mod.json") { expand(props) }

        // Mixin syntax compatibility level matches the version's Java target.
        inputs.property("mixinJava", mixinJava)
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }

    // Builds the active version into build/libs/<mod version>/.
    register<Copy>("buildAndCollect") {
        group = "build"
        from(loomx.modJar.map { it.archiveFile }, loomx.modSourcesJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/$modVersion"))
        dependsOn("build")
    }
}

// Rebuilds the native macOS keyboard fix (src/main/native/macmc.mm) into a
// universal dylib under src/main/resources. Only runs on macOS; the binary is
// committed so a non-macOS CI build can bundle it. Run after changing the native
// source: ./gradlew compileNatives
tasks.register<Exec>("compileNatives") {
    onlyIf { org.gradle.internal.os.OperatingSystem.current().isMacOsX }

    val nativeSrc = rootProject.file("src/main/native/macmc.mm")
    val nativeOut = rootProject.file("src/main/resources/natives/macmc.dylib")
    val javaHome = System.getenv("JAVA_HOME") ?: System.getProperty("java.home")

    inputs.file(nativeSrc)
    outputs.file(nativeOut)

    doFirst { nativeOut.parentFile.mkdirs() }

    commandLine(
        "clang++", "-dynamiclib", "-std=c++17",
        "-mmacosx-version-min=11.0",
        "-arch", "arm64", "-arch", "x86_64",
        "-I${javaHome}/include", "-I${javaHome}/include/darwin",
        "-framework", "Cocoa",
        "-o", nativeOut.absolutePath, nativeSrc.absolutePath,
    )
}
