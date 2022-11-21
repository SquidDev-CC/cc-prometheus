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

    // Core libraries
    implementation(libs.bundles.prometheus)
    compileOnly(libs.bundles.forgeConfig)
    // Extra mods
    compileOnly(libs.cct.forge) // We don't ship a common mod jar.
}
