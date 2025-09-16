plugins {
    id("java")
    id("org.springframework.boot") version "3.3.4" // 스프링부트 플러그인 추가
    id("io.spring.dependency-management") version "1.1.6" // 의존성 관리 플러그인
}

group = "com"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    // JUnit
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // MySQL
    implementation("com.mysql:mysql-connector-j:9.3.0")

    // AssertJ
    testImplementation("org.assertj:assertj-core:3.27.3")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.0")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}
