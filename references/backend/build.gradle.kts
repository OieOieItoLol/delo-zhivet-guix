tasks.compileJava {
    dependsOn("flywayMigrate") // 👈 Запускает миграции перед компиляцией
}

plugins {
    id("java")
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.5"
	id("org.flywaydb.flyway") version "11.8.0"
	id("org.springdoc.openapi-gradle-plugin") version "1.9.0"
}

buildscript {
	dependencies {
		classpath("org.flywaydb:flyway-database-postgresql:11.8.0")
	}
}

group = "delo-zhivet"
version = "1.0.0"

repositories {
    mavenCentral()
	mavenLocal()
}

flyway {
    url = System.getenv("PG_URL") // ?: "jdbc:postgresql://host.docker.internal:5432/tracker"
    user = System.getenv("PG_USERNAME") // ?: "site"
    password = System.getenv("PG_PASSWORD") // ?: "1234"

	locations  = arrayOf("filesystem:./src/main/resources/db/migration")
    mixed = true
	baselineOnMigrate = true
}

openApi {
	apiDocsUrl.set("http://localhost:9966/docs")
	outputDir.set(file("$rootDir"))
	outputFileName.set("api.json")
}

var trueSqlVersion = "3.0.0-beta11"

dependencies {
	annotationProcessor("net.truej:sql:$trueSqlVersion")
	annotationProcessor("org.postgresql:postgresql:42.7.3")

	testAnnotationProcessor("net.truej:sql:$trueSqlVersion")
	testAnnotationProcessor("org.postgresql:postgresql:42.7.3")

	implementation("net.truej:sql:$trueSqlVersion")
	implementation("org.jetbrains:annotations:24.0.0")
	implementation("org.postgresql:postgresql:42.7.3")
	implementation("com.zaxxer:HikariCP:5.1.0")

	implementation("org.springframework.boot:spring-boot-starter-web")

	testImplementation("org.flywaydb:flyway-core:10.17.2")
	testImplementation("org.flywaydb:flyway-database-postgresql:10.17.2")
	testImplementation("org.testcontainers:postgresql:1.20.1")
	testImplementation("org.testcontainers:junit-jupiter:1.20.1")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("org.flywaydb:flyway-database-postgresql")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
}
tasks.withType<JavaCompile> {
    options.isFork = true

    // 1. Возвращаем флаг подключения плагина
    options.compilerArgs?.add("-Xplugin:TrueSql")

    // 2. Оставляем диагностику (полезно для отладки)
    options.compilerArgs?.addAll(listOf("-XprintProcessorInfo", "-XprintRounds"))

    // 3. Оставляем JVM аргументы для доступа к внутренним API JDK
    options.forkOptions.jvmArgs?.addAll(
        listOf(
            "--add-opens", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
            "--add-opens", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
            "--add-opens", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
            "--add-opens", "jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
            "--add-opens", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
            "--add-opens", "jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
            "--add-opens", "jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
            "--add-opens", "jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
            "--add-opens", "java.base/jdk.internal.loader=ALL-UNNAMED",

            "--add-exports", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
            "--add-exports", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
            "--add-exports", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
            "--add-exports", "jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
            "--add-exports", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
            "--add-exports", "jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
            "--add-exports", "jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
            "--add-exports", "jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED"
        )
    )
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.test {
    useJUnitPlatform()
}
