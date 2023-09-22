/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/6.9.3/userguide/building_java_projects.html
 */


plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
     "com.github.johnrengelman.shadow"
    java

}

repositories {
    mavenLocal()

    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
        implementation("com.sap.customercheckout:ENV:2.16.2")
        implementation("com.google.code.gson:gson:2.10");
    // This dependency is used by the application.
    implementation("com.google.guava:guava:29.0-jre")
}

application {
    // Define the main class for the application.
    mainClass.set("com.trc.ccopromo.TrcPromoAddon")
    

}

tasks.jar {
    manifest {
        archiveBaseName.set("trcpromo")
        archiveFileName.set("trcpromo-2.0.jar")
        attributes(
            "pluginName" to "trc",
            "cashdeskPOSPlugin" to "com.trc.ccopromo.TrcPromoAddon",
            "cashDeskVersions" to "2.0 FP12, 2.0 FP13, 2.0 FP14, 2.0 FP15, 2.0 FP16, 2.0 FP17, n/a",
            "version" to "2.14.0",
            "pluginVersion" to "2.14.0"
        )
    }
        
}
