plugins {
    java
}

val mcVersion: String by extra
val modVersion: String by extra

group = "cc.tweaked"
version = modVersion

base.archivesName.convention("cc-prometheus-$mcVersion-${project.name}")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

tasks.jar {
    manifest {
        attributes(
            "Specification-Title" to "cc-prometheus",
            "Specification-Vendor" to "SquidDev",
            "Specification-Version" to "1",
            "Implementation-Title" to "cc-prometheus-${project.name}",
            "Implementation-Version" to modVersion,
            "Implementation-Vendor" to "SquidDev",
        )
    }
}

repositories {
    mavenCentral()

    maven("https://squiddev.cc/maven") {
        content {
            includeGroup("org.squiddev")
            includeGroup("cc.tweaked")
            includeModule("net.minecraftforge", "forgeconfigapiport-fabric")
        }
    }

    maven("https://maven.parchmentmc.org/") {
        name = "Parchment"
        content {
            includeGroup("org.parchmentmc.data")
        }
    }
}

tasks.withType(JavaCompile::class.java).configureEach {
    options.encoding = "UTF-8"
}
