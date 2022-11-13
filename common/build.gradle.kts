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
    implementation(libs.bundles.prometheus)
    compileOnly(libs.bundles.common)
}
