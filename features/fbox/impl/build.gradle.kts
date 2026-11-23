import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Fbox: doplňky forku nad Element X. Celý modul je náš kód.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

plugins {
    id("io.element.android-compose-library")
}

// Odesílatelé, jejichž zprávy ve tvaru dotazu dostanou tlačítka (Matrix ID oddělená čárkou).
// Nastavení při sestavení (gradle.properties mimo repo, nebo proměnná CI): ./gradlew ... -Pfbox.dotaz.odesilatele=@git:example.org,@jiny:example.org
val dotazOdesilatele: String = providers.gradleProperty("fbox.dotaz.odesilatele").orNull
    ?: "" // bez nastavení tlačítka nikomu nenabízíme

android {
    namespace = "cz.fbox.x.features.fbox.impl"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "DOTAZ_ODESILATELE", "\"$dotazOdesilatele\"")
    }
}

// Appyx uzly tu nejsou, generátor továren není potřeba.
setupDependencyInjection(generateNodeFactories = false)

dependencies {
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.matrix.api)
    implementation(projects.services.appnavstate.api)
    implementation(libs.coroutines.core)

    testCommonDependencies(libs)
}
