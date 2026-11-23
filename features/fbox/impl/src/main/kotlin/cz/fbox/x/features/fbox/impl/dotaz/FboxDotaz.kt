/*
 * Fbox: doplňky forku nad Element X.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package cz.fbox.x.features.fbox.impl.dotaz

/**
 * Dotaz dispečera rozpoznaný z textu zprávy.
 *
 * Tvar zprávy:
 * ```
 * D-4f2a · projekt #37
 * Export má být CSV, nebo XLSX?
 * 1) CSV   2) XLSX   3) obojí
 * Do 14:00, jinak platí 1.
 * https://git.example.org/tym/projekt/issues/37
 * ```
 */
data class FboxDotaz(
    /** Číslo dotazu včetně předpony, např. `D-4f2a`. */
    val id: String,
    /** Zbytek prvního řádku za oddělovačem, např. `projekt #37`. Může být prázdný. */
    val kontext: String,
    /** Text otázky (řádky mezi hlavičkou a možnostmi). Může být prázdný. */
    val otazka: String,
    /** Možnosti v pořadí, v jakém jsou ve zprávě. Vždy aspoň dvě. */
    val moznosti: List<Moznost>,
    /** Text lhůty, např. `14:00`. Null, když řádek se lhůtou chybí. */
    val lhuta: String?,
    /** Číslo výchozí volby, která platí po lhůtě. Null, když chybí nebo neodpovídá žádné možnosti. */
    val vychozi: Int?,
    /** První odkaz ve zprávě za možnostmi. */
    val odkaz: String?,
) {
    data class Moznost(
        val cislo: Int,
        val text: String,
    )
}
