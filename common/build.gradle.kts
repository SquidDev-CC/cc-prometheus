plugins {
    `java-library`
    id("java-convention")
    alias(libs.plugins.vanillaGradle)
}

val mcVersion: String by extra

minecraft {
    version(mcVersion)
}

dependencies {
    compileOnlyApi(libs.jsr305)
    compileOnly(libs.mixin)

    // Core libraries
    implementation(libs.bundles.prometheus)
    implementation(libs.bundles.nightConfig)
    // Extra mods
    compileOnly(libs.cct.forge) // There's no common mod jar, so use the Forge one.
}
