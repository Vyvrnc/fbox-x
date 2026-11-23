/*
 * Fbox: doplňky forku nad Element X.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package cz.fbox.x.features.fbox.impl.di

import cz.fbox.x.features.fbox.impl.dotaz.FboxDotazController
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo

/**
 * Přístup k našim službám z grafu aplikace. Compose kód si je vyzvedne přes `context.bindings<FboxBindings>()`,
 * takže jejich uzly a presentery nemusí o našem modulu vědět.
 */
@ContributesTo(AppScope::class)
interface FboxBindings {
    fun fboxDotazController(): FboxDotazController
}
