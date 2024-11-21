pluginManagement {
    repositories {
        google() {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()  // Asegúrate de que el repositorio de Google esté aquí
        mavenCentral()  // Añade Maven Central si es necesario
    }
}

rootProject.name = "Funcion-LoginRegistro"  // Nombre de tu proyecto
include(":app")  // Incluye el módulo principal de tu proyecto
