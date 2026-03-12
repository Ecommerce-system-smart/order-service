plugins {
	java
	id("org.springframework.boot") version "3.2.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.ecommerce"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenLocal()
	mavenCentral()
	maven {
		name = "GitHubPackages"
		url = uri("https://maven.pkg.github.com/Ecommerce-system-smart/common-service")
		credentials {
			// Logic: Nếu có biến môi trường (CI) thì dùng -> Nếu không thì tìm trong gradle.properties (Local)
			username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String?
			password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.key") as String?
		}
	}
}

extra["springCloudVersion"] = "2023.0.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.ecommerce:common:1.0.0")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
	// Thư viện giúp Spring Boot nói chuyện với Prometheus
	implementation("io.micrometer:micrometer-registry-prometheus")
	// --- PHẦN LOMBOK (Bắt buộc phải có cả 2 dòng) ---
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
	implementation("org.springframework.boot:spring-boot-starter-aop") // Cần AOP để annotation hoạt động
	// Giúp tạo ra Trace ID và Span ID
	implementation("io.micrometer:micrometer-tracing-bridge-brave")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("io.zipkin.reporter2:zipkin-reporter-brave")
	// Thêm vào trong dependencies { ... }
	implementation("org.springframework.kafka:spring-kafka:3.1.2")
	implementation("com.fasterxml.jackson.core:jackson-databind")
	// Nếu bạn dùng Lombok trong cả thư mục test (viết unit test) thì thêm dòng này:
	testAnnotationProcessor("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
