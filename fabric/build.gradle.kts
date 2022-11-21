plugins {
    `java-library`
    alias(libs.plugins.fabric.loom)
    id("java-convention")
}

val mcVersion: String by extra

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(
        loom.layered {
            officialMojangMappings()
            parchment(
                project.dependencies.create(
                    group = "org.parchmentmc.data",
                    name = "parchment-${libs.versions.parchmentMc.get()}",
                    version = libs.versions.parchment.get().toString(),
                    ext = "zip",
                ),
            )
        },
    )

    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    // Core libraries
    implementation(libs.bundles.prometheus)
    modImplementation(libs.bundles.forgeConfig)
    // Extra mods
    modCompileOnly(libs.cct.fabric)

    include(libs.bundles.prometheus)
    include(libs.bundles.forgeConfig)

    implementation(project(":common"))
}

loom {
    runs {
        configureEach {
            ideConfigGenerated(true)
        }

        named("client") {
            configName = "Fabric Client"
            runDir("run")
        }
        named("server") {
            configName = "Fabric Server"
            runDir("run/server")
        }
    }
}


tasks.compileJava { source(project(":common").sourceSets.main.get().allSource) }
tasks.processResources {
    from(project(":common").sourceSets.main.get().resources)

    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}
