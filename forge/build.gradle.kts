plugins {
    `java-library`
    alias(libs.plugins.shadow)
    alias(libs.plugins.forgeGradle)
    alias(libs.plugins.librarian)
    alias(libs.plugins.mixinGradle)
    id("java-convention")
}

val mcVersion: String by extra

minecraft {
    mappings("parchment", "${libs.versions.parchmentMc.get()}-${libs.versions.parchment.get()}-$mcVersion")

    runs {
        all {
            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")

            forceExit = false

            mods.register("ccprometheus") {
                source(sourceSets["main"])
                source(project(":common").sourceSets["main"])
            }
        }

        val client by registering {
            workingDirectory(file("run"))
        }

        val server by registering {
            workingDirectory(file("run/server"))
            arg("--nogui")
        }
    }
}

configurations {
    minecraftLibrary { extendsFrom(minecraftEmbed.get()) }
}

dependencies {
    "minecraft"("net.minecraftforge:forge:$mcVersion-${libs.versions.forge.get()}")

    compileOnly(project(":common"))

    // Core libraries
    minecraftEmbed(libs.bundles.prometheus) {
        jarJar.ranged(this, "[0.16.0,0.17.0]")
    }
    // Extra mods
    implementation(fg.deobf(libs.cct.forge.get()))
}

// Include classes/resources from :common
tasks.compileJava { source(project(":common").sourceSets.main.get().allSource) }
tasks.processResources { from(project(":common").sourceSets.main.get().resources) }

tasks.jar {
    finalizedBy("reobfJar")
    archiveClassifier.set("slim")
}

mixin {
    add(sourceSets.main.get(), "ccprometheus.mixins.refmap.json")
    config("ccprometheus.mixins.json")
}

reobf {
    create("shadowJar")
}

tasks.shadowJar {
    finalizedBy("reobfShadowJar")
    archiveClassifier.set("")

    relocate("io.prometheus", "cc.tweaked.prometheus.shadow.prometheus")
    configurations = listOf(project.configurations.minecraftEmbed.get())
    minimize()
    exclude("META-INF/maven/**")
}

tasks.assemble { dependsOn(tasks.shadowJar) }
