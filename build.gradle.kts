plugins {
    application
}

group = "com.github.jbench"
version = "1.0"

val bcVersion = "1.76"
val jmhVersion = "1.37"
val mainClassName = "org.openjdk.jmh.Main"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.bouncycastle:bcprov-jdk18on:${bcVersion}")

    implementation("org.openjdk.jmh:jmh-core:${jmhVersion}")
    implementation("org.openjdk.jmh:jmh-generator-annprocess:${jmhVersion}")

    // Used by IDE
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:${jmhVersion}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withSourcesJar()
}

application {
    mainClass.set(mainClassName)
}

var uberJar = task("uberJar", type = Jar::class) {
    archiveClassifier.set("uber")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(mapOf("Main-Class" to application.mainClass))
    }

    val contents = configurations.runtimeClasspath.get()
        .map {
            if (it.isDirectory) {
                it
            } else {
                zipTree(it)
            }
        } + sourceSets.main.get().output
    from(contents) {
        exclude("META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA")
    }

    dependsOn(":compileJava", ":processResources")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    compileTestJava {
        options.encoding = "UTF-8"
    }

    register("jmh", type=JavaExec::class) {
        mainClass.set(mainClassName)
        classpath(sourceSets["main"].runtimeClasspath)
    }

    "build" {
        dependsOn(uberJar)
    }
}
