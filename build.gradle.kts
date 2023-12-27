plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.kousenit"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    implementation("org.apache.httpcomponents.client5:httpclient5:5.3")
    implementation("com.google.code.gson:gson:2.10.1")
    // implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("org.apache.logging.log4j:log4j-core:2.22.0")

    // Apache POI dependencies
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    // JavaFX dependencies
    implementation("org.openjfx:javafx-controls:17.0.1")
    implementation("org.openjfx:javafx-fxml:17.0.1")

    implementation("com.github.umjammer:jlayer:1.0.2")

    testImplementation(platform("org.junit:junit-bom:5.10.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.vintage:junit-vintage-engine")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.8.0")
}

tasks.test {
    useJUnitPlatform()
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.media")
}

application {
    // mainClass.set("com.kousenit.picogen.ImageDownloader")
    mainClass.set("com.kousenit.openai.ImageCarousel")
}
