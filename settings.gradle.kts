pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()

        maven("https://maven.neoforged.net/releases") {
            name = "NeoForge"
            content {
                includeGroup("net.minecraftforge")
                includeGroup("net.neoforged")
                includeGroup("net.neoforged.gradle")
                includeModule("codechicken", "DiffPatch")
                includeModule("net.covers1624", "Quack")
            }
        }

        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
            content {
                includeGroup("fabric-loom")
                includeGroup("net.fabricmc")
            }
        }

        maven("https://maven.squiddev.cc") {
            name = "SquidDev"
            content {
                includeGroup("cc.tweaked.vanilla-extract")
            }
        }
    }
}

rootProject.name = "cc-prometheus"
include("common", "fabric", "forge")
