plugins {
    `java-library`
    `maven-publish`
    id("net.neoforged.moddev") version "2.0.147"
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()
base { archivesName = "fluidloggable-neoforge" }
java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    withSourcesJar()
}
repositories { mavenCentral() }

// Optional integrations remain in source for the next port stage.
sourceSets.main {
    java.exclude("com/moigferdsrte/fluidloggable/compat/**",
        "com/moigferdsrte/fluidloggable/config/FluidloggableConfigMenu.java")
    resources.exclude("fluidloggable-*.mixins.json")
}
val gametest by sourceSets.creating {
    java.exclude("**/FluidloggableClientGameTest.java", "**/ComfortsBlockGameTest.java", "**/FarmersDelightBlockGameTest.java")
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}
configurations[gametest.implementationConfigurationName].extendsFrom(configurations.implementation.get())
configurations[gametest.runtimeOnlyConfigurationName].extendsFrom(configurations.runtimeOnly.get())

neoForge {
    version = providers.gradleProperty("neo_version").get()
    mods {
        create("fluidloggable") { sourceSet(sourceSets.main.get()) }
        create("fluidloggable_gametest") { sourceSet(gametest) }
    }
    addModdingDependenciesTo(gametest)
    unitTest {
        enable()
        testedMod = mods.getByName("fluidloggable")
        loadedMods = setOf(mods.getByName("fluidloggable"))
    }
    runs {
        create("client") { client(); loadedMods = setOf(mods.getByName("fluidloggable")) }
        create("server") { server(); programArgument("--nogui"); loadedMods = setOf(mods.getByName("fluidloggable")) }
        create("clientSmoke") {
            client()
            sourceSet = gametest
            gameDirectory = file("run/neoforge-client-smoke")
            systemProperty("fluidloggable.clientSmokeTest", "true")
            loadedMods = setOf(mods.getByName("fluidloggable"), mods.getByName("fluidloggable_gametest"))
        }
        create("gameTestServer") {
            type = "gameTestServer"
            sourceSet = gametest
            gameDirectory = file("run/neoforge-gametest")
            systemProperty("neoforge.enabledGameTestNamespaces", "fluidloggable_gametest")
            loadedMods = setOf(mods.getByName("fluidloggable"), mods.getByName("fluidloggable_gametest"))
        }
    }

}
dependencies {
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
tasks.test { useJUnitPlatform() }
tasks.withType<JavaCompile>().configureEach {
    options.release = 25
    options.encoding = "UTF-8"
}
tasks.processResources {
    inputs.property("version", project.version)
    filesMatching(listOf("META-INF/neoforge.mods.toml", "fluidloggable-version.properties")) {
        expand("version" to project.version)
    }
}
tasks.jar {
    from("CREDITS.md")
    from("LICENSE") { rename { "${it}_fluidloggable" } }
}
publishing {
    publications {
        register<MavenPublication>("mavenJava") { from(components["java"]) }
    }
}
