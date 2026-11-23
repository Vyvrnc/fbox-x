/*
 * Fbox: doplňky forku nad Element X.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package cz.fbox.x.features.fbox.impl.dotaz

/**
 * Rozpozná dotaz dispečera z prostého textu zprávy. Čistý Kotlin bez závislostí, ať se dá testovat samostatně.
 *
 * Záměrně přísný: když zpráva tvaru neodpovídá, vrací null a zpráva se vykreslí jako obyčejný text.
 */
object FboxDotazParser {
    /** Rychlá předkontrola, která nestojí nic: volá se pro každou zprávu v časové ose. */
    fun muzeBytDotaz(body: String): Boolean = body.startsWith(PREDPONA)

    fun parse(body: String): FboxDotaz? {
        if (!muzeBytDotaz(body) || body.length > MAX_DELKA) return null
        val radky = body.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (radky.size < 2) return null

        val hlavicka = HLAVICKA.matchEntire(radky[0]) ?: return null
        val id = PREDPONA + hlavicka.groupValues[1]
        val kontext = hlavicka.groupValues[2].trim()

        // Možnosti: souvislý blok řádků začínajících „N)“. Na řádku jich může být víc.
        val prvniMoznosti = radky.indexOfFirst { ZACATEK_MOZNOSTI.containsMatchIn(it) }
        if (prvniMoznosti < 1) return null
        var zaMoznostmi = prvniMoznosti
        val moznosti = mutableListOf<FboxDotaz.Moznost>()
        while (zaMoznostmi < radky.size && ZACATEK_MOZNOSTI.containsMatchIn(radky[zaMoznostmi])) {
            moznosti += rozdelMoznosti(radky[zaMoznostmi])
            zaMoznostmi++
        }
        if (moznosti.size < 2 || moznosti.size > MAX_MOZNOSTI) return null
        if (moznosti.any { it.text.isEmpty() }) return null
        if (moznosti.map { it.cislo }.toSet().size != moznosti.size) return null

        val otazka = radky.subList(1, prvniMoznosti).joinToString("\n")
        val zbytek = radky.subList(zaMoznostmi, radky.size)

        val lhutaShoda = zbytek.firstNotNullOfOrNull { LHUTA.matchEntire(it) }
        val vychozi = lhutaShoda?.groupValues?.get(2)?.toIntOrNull()?.takeIf { cislo -> moznosti.any { it.cislo == cislo } }
        val odkaz = zbytek.firstNotNullOfOrNull { ODKAZ.find(it)?.value }

        return FboxDotaz(
            id = id,
            kontext = kontext,
            otazka = otazka,
            moznosti = moznosti,
            lhuta = lhutaShoda?.groupValues?.get(1)?.trim(),
            vychozi = vychozi,
            odkaz = odkaz,
        )
    }

    private fun rozdelMoznosti(radek: String): List<FboxDotaz.Moznost> {
        val znacky = ZNACKA_MOZNOSTI.findAll(radek).toList()
        return znacky.mapIndexed { index, znacka ->
            val konec = znacky.getOrNull(index + 1)?.range?.first ?: radek.length
            FboxDotaz.Moznost(
                cislo = znacka.groupValues[1].toInt(),
                text = radek.substring(znacka.range.last + 1, konec).trim(),
            )
        }
    }

    private const val PREDPONA = "D-"
    private const val MAX_DELKA = 4000
    private const val MAX_MOZNOSTI = 9

    // „D-4f2a“ nebo „D-4f2a · projekt #37“; oddělovač je tečka uprostřed, odrážka, pomlčka nebo svislítko.
    private val HLAVICKA = Regex("""^D-([0-9A-Za-z]{2,12})(?:\s*[·•|–—-]\s*(.*))?$""")

    // Řádek začíná „1) “.
    private val ZACATEK_MOZNOSTI = Regex("""^\d{1,2}\)\s""")

    // Značka možnosti na začátku řádku, nebo uprostřed řádku po aspoň dvou mezerách: „1) CSV   2) XLSX“.
    // Dvě mezery odliší další možnost od závorky v textu („starou (verze 2) nechat“).
    private val ZNACKA_MOZNOSTI = Regex("""(?:^|(?<=\s{2}))(\d{1,2})\)\s""")

    // „Do 14:00, jinak platí 1.“
    private val LHUTA = Regex("""^Do\s+(.+?),\s*jinak\s+platí\s+(\d{1,2})\.?$""", RegexOption.IGNORE_CASE)

    private val ODKAZ = Regex("""https?://\S+""")
}
