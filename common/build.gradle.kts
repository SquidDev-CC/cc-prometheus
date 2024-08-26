plugins {
    `java-library`
    id("java-convention")
    alias(libs.plugins.vanillaExtract)
}

val mcVersion: String by extra

minecraft {
    version(mcVersion)

    mappings {
        parchment(libs.versions.parchmentMc.get().toString(), libs.versions.parchment.get().toString())
    }

    unpick(libs.yarn)
}

repositories {
    maven("https://maven.neoforged.net/") {
        content {
            includeModule("org.spongepowered", "mixin")
        }
    }
}

dependencies {
    compileOnlyApi(libs.jsr305)
    compileOnly(libs.mixin)

    // Core libraries
    implementation(libs.bundles.prometheus)
    implementation(libs.bundles.nightConfig)
    // Extra mods
    compileOnly(libs.cct.core) // This feels like a bug! It should be a transitive dep of common.
    compileOnly(libs.cct.common)
}
