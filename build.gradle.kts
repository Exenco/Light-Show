import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.3.0"
}

group = "net.exenco.lightshow"
version = "1.0.0"
description = "Professional, mod-free server light show system."

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
}

tasks {
    compileJava {
        options.release = 21
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
}

bukkitPluginYaml {
    main = "net.exenco.lightshow.LightShowPlugin"
    load = BukkitPluginYaml.PluginLoadOrder.POSTWORLD
    authors.add("Exenco")
    apiVersion = "1.21.11"
}
