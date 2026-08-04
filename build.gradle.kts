import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "com.tangjja"
version = "2.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.2.4")
        bundledPlugin("com.intellij.java")
        testFramework(TestFrameworkType.Platform)
    }

    implementation("com.github.librepdf:openpdf:3.0.5")
    implementation("commons-io:commons-io:2.22.0")
    implementation("org.apache.commons:commons-lang3:3.20.0")
    implementation("org.yaml:snakeyaml:2.6")

    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    pluginConfiguration {
        version = project.version.toString()
        ideaVersion {
            sinceBuild = "242"
            untilBuild = provider { null }
        }
        changeNotes = """
            <h3>2.0.0-SNAPSHOT</h3>
            <ul>
                <li>Java 基线升级到 21，最低支持 IntelliJ IDEA 2024.2。</li>
                <li>升级构建体系、PDF、数据库与配置解析依赖。</li>
            </ul>
        """.trimIndent()
    }

    pluginVerification {
        ides {
            create(IntelliJPlatformType.IntellijIdeaCommunity, "2024.2.4")
            create(IntelliJPlatformType.IntellijIdeaCommunity, "2025.1.7")
            create(IntelliJPlatformType.IntellijIdea, "2026.2.0.1")
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
}

tasks.withType<Test>().configureEach {
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named("build") {
    dependsOn(tasks.named("buildPlugin"))
}
