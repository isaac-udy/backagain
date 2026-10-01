plugins {
    // Declared once here so every subproject shares one plugin classloader.
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.kotlinKsp) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.udytilsArchitecture) apply false
    alias(libs.plugins.udytilsPostgres) apply false
}
