// settings.gradle.kts (سطح ریشه‌ی ریپازیتوری)
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AidenAssistant"
include(":app")
project(":app").projectDir = file("app/app")
