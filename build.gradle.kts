plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.3.72"

    // Apply the application plugin to add support for building a CLI application.
    application
}

//apply(plugin = "com.squareup.sqldelight")

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("no.tornado:tornadofx:1.7.20")

    implementation("com.github.andrewoma.kwery:core:0.17")
    implementation("org.xerial:sqlite-jdbc:3.31.1")
    implementation("khttp:khttp:1.0.0")
    implementation("com.nfeld.jsonpathlite:json-path-lite:1.1.0")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
//    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.5.2")
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
}

application {
    // Define the main class for the application.
    mainClassName = "com.example.demo.app.MyApp"
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}