pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()

        maven("https://maven.minecraftforge.net") {
            name = "Forge"
            content {
                includeGroup("net.minecraftforge")
                includeGroup("net.minecraftforge.gradle")
            }
        }

        maven("https://maven.parchmentmc.org") {
            name = "Librarian"
            content {
                includeGroupByRegex("^org\\.parchmentmc.*")
            }
        }

        maven("https://repo.spongepowered.org/repository/maven-public/") {
            name = "Sponge"
            content {
                includeGroup("org.spongepowered")
                includeGroup("org.spongepowered.gradle.vanilla")
            }
        }

        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
            content {
                includeGroup("fabric-loom")
                includeGroup("net.fabricmc")
            }
        }
    }
}

rootProject.name = "cc-prometheus"
include("common", "fabric", "forge")
