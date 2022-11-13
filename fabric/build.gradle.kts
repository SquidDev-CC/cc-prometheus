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

    modImplementation(libs.bundles.fabric)
    implementation(libs.bundles.prometheus)
    include(libs.bundles.prometheus)

    implementation(project(":common"))

//     implementation group: 'com.google.code.findbugs', name: 'jsr305', version: '3.0.1'
//
//     implementation project(":common")
//
//     modImplementation ("cc.tweaked:cc-tweaked-${minecraft_version}-fabric:${cct_version}")
//
//     // IDK how Fabric config works (so much seems to be client-only??), so just copy CC:R.
//     implementation 'com.electronwill.night-config:toml:3.6.5'
//     include 'com.electronwill.night-config:core:3.6.5'
//     include 'com.electronwill.night-config:toml:3.6.5'
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
