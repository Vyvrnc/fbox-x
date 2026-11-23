/*
 * Fbox: doplňky forku nad Element X.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package cz.fbox.x.features.fbox.impl.dotaz

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FboxDotazParserTest {
    private val vzor = """
        D-4f2a · projekt #37
        Export má být CSV, nebo XLSX?
        1) CSV   2) XLSX   3) obojí
        Do 14:00, jinak platí 1.
        https://git.example.org/tym/projekt/issues/37
    """.trimIndent()

    @Test
    fun `vzorová zpráva z návrhu dispečera se rozpozná celá`() {
        val dotaz = FboxDotazParser.parse(vzor)
        assertThat(dotaz).isEqualTo(
            FboxDotaz(
                id = "D-4f2a",
                kontext = "projekt #37",
                otazka = "Export má být CSV, nebo XLSX?",
                moznosti = listOf(
                    FboxDotaz.Moznost(1, "CSV"),
                    FboxDotaz.Moznost(2, "XLSX"),
                    FboxDotaz.Moznost(3, "obojí"),
                ),
                lhuta = "14:00",
                vychozi = 1,
                odkaz = "https://git.example.org/tym/projekt/issues/37",
            )
        )
    }

    @Test
    fun `možnosti mohou být každá na vlastním řádku`() {
        val dotaz = FboxDotazParser.parse(
            """
            D-0b71 · provoz #41
            Jak naložit s GoodWe v HA?
            1) nechat být
            2) vrátit starou integraci
            Do 9:30, jinak platí 2.
            """.trimIndent()
        )
        assertThat(dotaz?.moznosti).containsExactly(
            FboxDotaz.Moznost(1, "nechat být"),
            FboxDotaz.Moznost(2, "vrátit starou integraci"),
        ).inOrder()
        assertThat(dotaz?.vychozi).isEqualTo(2)
        assertThat(dotaz?.odkaz).isNull()
    }

    @Test
    fun `otázka může mít víc řádků a konce řádků CRLF`() {
        val dotaz = FboxDotazParser.parse("D-aa11 · x #1\r\nPrvní řádek.\r\nDruhý řádek?\r\n1) ano   2) ne\r\n")
        assertThat(dotaz?.otazka).isEqualTo("První řádek.\nDruhý řádek?")
        assertThat(dotaz?.moznosti).hasSize(2)
        assertThat(dotaz?.lhuta).isNull()
        assertThat(dotaz?.vychozi).isNull()
    }

    @Test
    fun `hlavička bez kontextu stačí`() {
        val dotaz = FboxDotazParser.parse("D-4f2a\nAno, nebo ne?\n1) ano  2) ne")
        assertThat(dotaz?.id).isEqualTo("D-4f2a")
        assertThat(dotaz?.kontext).isEmpty()
    }

    @Test
    fun `závorka uvnitř textu možnosti není nová možnost`() {
        val dotaz = FboxDotazParser.parse("D-4f2a · a #2\nKterou?\n1) starou (verze 2) nechat   2) novou")
        assertThat(dotaz?.moznosti).containsExactly(
            FboxDotaz.Moznost(1, "starou (verze 2) nechat"),
            FboxDotaz.Moznost(2, "novou"),
        ).inOrder()
    }

    @Test
    fun `výchozí volba mimo nabídku se zahodí, lhůta zůstane`() {
        val dotaz = FboxDotazParser.parse("D-4f2a · a #2\nKterou?\n1) a   2) b\nDo zítřka 8:00, jinak platí 7.")
        assertThat(dotaz?.lhuta).isEqualTo("zítřka 8:00")
        assertThat(dotaz?.vychozi).isNull()
    }

    @Test
    fun `běžné zprávy nejsou dotaz`() {
        assertThat(FboxDotazParser.parse("Ahoj, jak se máš?")).isNull()
        assertThat(FboxDotazParser.parse("")).isNull()
        assertThat(FboxDotazParser.parse("D-den byl 6. června")).isNull()
        assertThat(FboxDotazParser.parse("Ráno 17. 11.\nČeká na tebe: 2\n1) a 2) b")).isNull()
    }

    @Test
    fun `dotaz bez možností nebo s jedinou možností se nerozpozná`() {
        assertThat(FboxDotazParser.parse("D-4f2a · projekt #37\nJen informace bez voleb.")).isNull()
        assertThat(FboxDotazParser.parse("D-4f2a · projekt #37\nOtázka?\n1) jediná")).isNull()
    }

    @Test
    fun `opakované číslo možnosti se nerozpozná`() {
        assertThat(FboxDotazParser.parse("D-4f2a · a #2\nKterou?\n1) a   1) b")).isNull()
    }

    @Test
    fun `možnosti hned na prvním řádku se nerozpoznají`() {
        assertThat(FboxDotazParser.parse("1) a   2) b")).isNull()
    }

    @Test
    fun `předkontrola propustí jen zprávy začínající předponou`() {
        assertThat(FboxDotazParser.muzeBytDotaz(vzor)).isTrue()
        assertThat(FboxDotazParser.muzeBytDotaz(" D-4f2a")).isFalse()
        assertThat(FboxDotazParser.muzeBytDotaz("d-4f2a")).isFalse()
    }
}
