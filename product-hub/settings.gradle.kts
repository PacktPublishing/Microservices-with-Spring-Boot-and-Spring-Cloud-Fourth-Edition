plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "product-hub"

include(
	"product-service",
	"recommendation-service",
	"review-service",
	"product-composite-service",
)
