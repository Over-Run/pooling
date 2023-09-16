plugins {
    java
    idea
    signing
    `maven-publish`
}

val projGroupId: String by rootProject
val projArtifactId: String by rootProject
val projName: String by rootProject
val projVersion: String by rootProject
val projDesc: String by rootProject
val projVcs: String by rootProject
val projBranch: String by rootProject
val orgName: String by rootProject
val orgUrl: String by rootProject
val developers: String by rootProject

group = projGroupId
version = projVersion

repositories {
    mavenCentral()
    maven { url = uri("https://maven.aliyun.com/repository/central") }
    // temporary maven repositories
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/releases") }
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots") }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

val targetJavaVersion = 17
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
    withJavadocJar()
    withSourcesJar()
}

tasks.named<Javadoc>("javadoc") {
    isFailOnError = false
    options {
        encoding = "UTF-8"
        locale = "en_US"
        windowTitle = "$projName $projVersion Javadoc"
        if (this is StandardJavadocDocletOptions) {
            charSet = "UTF-8"
            isAuthor = true
            links("https://docs.oracle.com/en/java/javase/$targetJavaVersion/docs/api/")
        }
    }
}

tasks.named<Jar>("jar") {
    manifestContentCharset = "utf-8"
    metadataCharset = "utf-8"
    from("LICENSE")
    manifest.attributes(
        "Specification-Title" to projName,
        "Specification-Vendor" to orgName,
        "Specification-Version" to "0",
        "Implementation-Title" to projName,
        "Implementation-Vendor" to orgName,
        "Implementation-Version" to archiveVersion
    )
    archiveBaseName.set(projArtifactId)
}

tasks.named<Jar>("sourcesJar") {
    dependsOn(tasks["classes"])
    archiveClassifier.set("sources")
//    from(sourceSets.main.allSource, "LICENSE")
}

tasks.named<Jar>("javadocJar") {
    val javadoc by tasks
    dependsOn(javadoc)
    archiveClassifier.set("javadoc")
    from(javadoc, "LICENSE")
}

artifacts {
    archives(tasks["javadocJar"])
    archives(tasks["sourcesJar"])
}

publishing.publications {
    register<MavenPublication>("mavenJava") {
        groupId = projGroupId
        artifactId = projArtifactId
        version = projVersion
        description = projDesc
        from(components["java"])
        pom {
            name = projName
            description = projDesc
            url.set("https://github.com/$projVcs")
            licenses {
                license {
                    name.set("MIT")
                    url.set("https://raw.githubusercontent.com/$projVcs/$projBranch/LICENSE")
                }
            }
            organization {
                name = orgName
                url = orgUrl
            }
            developers {
                val prop = developers.split(',')
                prop.map { it.split(':', limit = 3) }
                    .forEach {
                        developer {
                            id.set(it[0])
                            name.set(it[1])
                            email.set(it[2])
                        }
                    }
            }
            scm {
                connection.set("scm:git:https://github.com/${projVcs}.git")
                developerConnection.set("scm:git:https://github.com/${projVcs}.git")
                url.set("https://github.com/${projVcs}.git")
            }
        }
    }
}
// You have to add 'OSSRH_USERNAME', 'OSSRH_PASSWORD', 'signing.keyId',
// 'signing.password' and 'signing.secretKeyRingFile' to
// GRADLE_USER_HOME/gradle.properties
publishing.repositories {
    maven {
        name = "OSSRH"
        credentials {
            username = project.findProperty("OSSRH_USERNAME").toString()
            password = project.findProperty("OSSRH_PASSWORD").toString()
        }
        url = uri(
            if (projVersion.endsWith("-SNAPSHOT"))
                "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            else "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
        )
    }
}

signing {
    if (!projVersion.endsWith("-SNAPSHOT") && System.getProperty("gpg.signing", "true").toBoolean())
        sign(publishing.publications["mavenJava"])
}

idea.module.inheritOutputDirs = true
