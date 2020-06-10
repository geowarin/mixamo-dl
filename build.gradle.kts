plugins {
  id("org.jetbrains.kotlin.jvm") version "1.3.72"
  application
  id("com.github.johnrengelman.shadow") version "5.2.0"
  id("edu.sc.seis.launch4j") version "2.4.6"
}

repositories {
  jcenter()
  maven("https://jitpack.io")
}

dependencies {
  // Align versions of all Kotlin components
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("no.tornado:tornadofx:1.7.20")
  implementation("no.tornado:tornadofx-controlsfx:0.1.1")

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
  testImplementation("org.testfx:testfx-core:4.0.16-alpha")
  testImplementation("com.google.jimfs:jimfs:1.1")
}

application {
  // Define the main class for the application.
  mainClassName = "com.example.demo.app.MyAppKt"
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

tasks.withType<edu.sc.seis.launch4j.tasks.DefaultLaunch4jTask> {
  bundledJrePath = "C:/Users/perso/dev/tools/jdk-8u241-windows-x64"
  bundledJre64Bit = true
//  headerType = "gui"
  headerType = "console"

  setCopyConfigurable(project.tasks.shadowJar.get().outputs.files)
  jar = "lib/${project.tasks.shadowJar.get().archiveName}"
}