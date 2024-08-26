plugins {
    `java-library`
    alias(libs.plugins.modDevGradle)
    id("java-convention")
}

val mcVersion: String by extra

neoForge {
    version = libs.versions.neoForge

    parchment {
        minecraftVersion = libs.versions.parchmentMc
        mappingsVersion = libs.versions.parchment
    }

    runs {
        register("client") { client() }
        register("server") { server() }
    }

    mods {
        register("ccprometheus") {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    compileOnly(project(":common"))

    // Core libraries
    implementation(libs.bundles.prometheus)
    additionalRuntimeClasspath(libs.bundles.prometheus)
    jarJar(libs.bundles.prometheus)

    // Extra mods
    implementation(libs.cct.forge)
}

// Include classes/resources from :common
tasks.compileJava { source(project(":common").sourceSets.main.get().allSource) }
tasks.processResources { from(project(":common").sourceSets.main.get().resources) }
