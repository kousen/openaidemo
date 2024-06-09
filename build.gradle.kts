plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
    alias(libs.plugins.versions)
    alias(libs.plugins.version.catalog.update)
}

group = "com.kousenit"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation(libs.httpclient)
    implementation(libs.gson)
    implementation(libs.slf4j)
    implementation(libs.log4j)

    // Apache POI dependencies
    implementation(libs.bundles.poi)
    implementation("org.apache.commons:commons-compress:1.26.1") // avoid security issue in poi version

    // JavaFX dependencies
    implementation("org.openjfx:javafx-controls:21.0.2")
    implementation("org.openjfx:javafx-fxml:21.0.2")

    // JLayer dependencies for playing mp3 files
    implementation(libs.jlayer)

    // Testing
    testImplementation(libs.assertj)
    testImplementation(libs.bundles.junit)
    testImplementation(libs.bundles.mockito)
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.media")
}

application {
    // mainClass.set("com.kousenit.picogen.ImageDownloader")
    mainClass.set("com.kousenit.openai.ImageCarousel")
}
