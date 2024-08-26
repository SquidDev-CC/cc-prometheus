plugins {
    java
}

val mcVersion: String by extra
val modVersion: String by extra

group = "cc.tweaked"
version = modVersion

base.archivesName.convention("cc-prometheus-$mcVersion-${project.name}")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
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

    maven("https://maven.squiddev.cc") {
        content {
            includeGroup("cc.tweaked")
        }
    }
}

tasks.withType(JavaCompile::class.java).configureEach {
    options.encoding = "UTF-8"
}
