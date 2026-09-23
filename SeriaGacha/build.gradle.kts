plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.23"
}

group = "id.seria"
version = "2.0.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")                                         // Vault, ItemsAdder, etc.
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://mvn.lumine.io/repository/maven-public/")             // ModelEngine
    maven("https://repo.rosewooddev.io/repository/public/")             // PlayerPoints
    maven("https://nexus.bencodez.com/repository/maven-public/")        // VotingPlugin
    maven("https://repo.momirealms.net/releases/")                      // CraftEngine
    maven("https://repo.ssomar.com/releases/") {             // SCore, ExecutableItems
        metadataSources { artifact() }                       // POM metadata mismatches — resolve JARs only
    }
    maven("https://repo.nightexpressdev.com/releases/")                 // ExcellentEconomy
    maven("https://repo.nexomc.com/releases/")                          // Nexo
}

dependencies {
    // Paper dev-bundle: provides paper-api, paper-server (NMS), adventure, authlib, brigadier, etc.
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")

    // Compile-only annotations
    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")
    compileOnly("org.jetbrains:annotations:24.1.0")

    // Server libraries not included in dev-bundle
    compileOnly("commons-lang:commons-lang:2.6")
    compileOnly("commons-io:commons-io:2.16.1")
    compileOnly("net.md-5:bungeecord-chat:1.20-R0.2")

    // Hook APIs — all provided at runtime by their respective plugins
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("io.github.toxicity188:bettermodel-api:2.2.0")
    compileOnly("io.github.toxicity188:bettermodel-bukkit-api:2.2.0")
    compileOnly("net.momirealms:craft-engine-core:26.9.1")
    compileOnly("net.momirealms:craft-engine-bukkit:26.9.1")
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.9")
    compileOnly("org.black_ixx:playerpoints:3.3.5")
    compileOnly("com.bencodez:votingplugin:7.1.1")
    compileOnly("com.ssomar:SCore:5.26.9.17")                { isTransitive = false }
    compileOnly("com.ssomar.executableitems:ExecutableItems-API:7.26.9.17") { isTransitive = false }
    compileOnly("su.nightexpress.excellenteconomy:ExcellentEconomy:2.8.0")  { isTransitive = false }
    compileOnly("com.github.LoneDev6:itemsadder-api:3.6.1")  { isTransitive = false }
    compileOnly("com.nexomc:nexo:1.0.0")                     { isTransitive = false }

    // Ebean, ASM, metrics — provided by server or shaded in-tree
    compileOnly("org.avaje:ebean:2.8.1")
    compileOnly("org.ow2.asm:asm:9.7.1")
    compileOnly("org.ow2.asm:asm-commons:9.7.1")
    compileOnly("org.ow2.asm:asm-tree:9.7.1")
    compileOnly("org.ow2.asm:asm-analysis:9.7.1")
    compileOnly("org.ow2.asm:asm-util:9.7.1")
    compileOnly("io.dropwizard.metrics:metrics-core:4.2.19")
    compileOnly("io.dropwizard.metrics:metrics-healthchecks:4.2.19")
    compileOnly("io.micrometer:micrometer-core:1.12.5")
    compileOnly("io.prometheus:simpleclient:0.16.0")
    compileOnly("org.hibernate:hibernate-core:5.6.15.Final")
    compileOnly("org.javassist:javassist:3.29.2-GA")
}

// ── Source sets ──────────────────────────────────────────────────────────────
sourceSets.main {
    java.srcDir("src/stubs/java")
}

// ── Compiler: exclude legacy NMS, migrations, old ModelEngine from compilation ──
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:none")
    options.sourcepath = files("work/empty-sourcepath")
    options.encoding = "UTF-8"
}

// ── JAR task ────────────────────────────────────────────────────────────────
tasks.jar {
    archiveBaseName.set("SeriaGacha")
    archiveVersion.set("2.0.0")
    archiveClassifier.set("")

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    manifest { from("src/main/resources/META-INF/MANIFEST.MF") }

    // Merge pre-compiled legacy bytecode into the JAR
    from("work/extracted-raw") {
        include(
            "id/seria/gacha/lib/common/nms/v1_*/**",
            "id/seria/gacha/lib/common/nms/v26_R1/**",
            "id/seria/gacha/lib/common/modelengine/v4_0_3/**",
            "id/seria/gacha/lib/common/modelengine/v4_0_8/**",
            "id/seria/gacha/thirdparty/impl/**",
            "id/seria/gacha/lib/xseries/profiles/gameprofile/OldGameProfile.class",
            "id/seria/gacha/lib/xseries/profiles/gameprofile/property/OldProperty.class",
        )
    }

    // Strip compile-only stubs from production JAR
    exclude(
        "io/lumine/**",
        "me/arcaniax/**",
        "net/Indyuce/**",
        "com/willfp/**",
        "su/nightexpress/nightcore/**",
    )
}

// Resource filtering must stay OFF (tokens like %player%, PEM keys)
tasks.processResources {
    filteringCharset = "UTF-8"
    // No expand/filter — resources are copied verbatim
}
