/*
 * Fbox: doplňky forku nad Element X.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package cz.fbox.x.features.fbox.impl.dotaz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton

/**
 * Tlačítka možností pod zprávou s dotazem.
 *
 * Odpověď se odesílá na dvě klepnutí (výběr, potvrzení), aby ji neodeslal dotyk při posouvání časové osy.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FboxDotazView(
    dotaz: FboxDotaz,
    stav: FboxOdpovedStav,
    onOdpoved: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var vybrano by rememberSaveable(dotaz.id) { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        when (stav) {
            is FboxOdpovedStav.Odeslano -> Popisek("Odesláno: ${popisMoznosti(dotaz, stav.cislo)}")
            is FboxOdpovedStav.Odesila -> Popisek("Odesílám: ${popisMoznosti(dotaz, stav.cislo)}…")
            is FboxOdpovedStav.Zadna, is FboxOdpovedStav.Chyba -> {
                if (stav is FboxOdpovedStav.Chyba) {
                    Popisek("Odpověď ${stav.cislo} se nepodařilo odeslat: ${stav.zprava}", chyba = true)
                }
                val vybranaMoznost = dotaz.moznosti.firstOrNull { it.cislo == vybrano }
                if (vybranaMoznost == null) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        dotaz.moznosti.forEach { moznost ->
                            val text = "${moznost.cislo}) ${moznost.text}"
                            if (moznost.cislo == dotaz.vychozi) {
                                // Výchozí volba (platí po lhůtě) je zvýrazněná.
                                Button(text = text, onClick = { vybrano = moznost.cislo }, size = ButtonSize.Small)
                            } else {
                                OutlinedButton(text = text, onClick = { vybrano = moznost.cislo }, size = ButtonSize.Small)
                            }
                        }
                    }
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Button(
                            text = "Odeslat: ${vybranaMoznost.cislo}) ${vybranaMoznost.text}",
                            onClick = {
                                vybrano = null
                                onOdpoved(vybranaMoznost.cislo)
                            },
                            size = ButtonSize.Small,
                        )
                        TextButton(text = "Zpět", onClick = { vybrano = null }, size = ButtonSize.Small)
                    }
                }
            }
        }
    }
}

@Composable
private fun Popisek(text: String, chyba: Boolean = false) {
    Text(
        text = text,
        style = ElementTheme.typography.fontBodySmRegular,
        color = if (chyba) ElementTheme.colors.textCriticalPrimary else ElementTheme.colors.textSecondary,
    )
}

private fun popisMoznosti(dotaz: FboxDotaz, cislo: Int): String {
    val text = dotaz.moznosti.firstOrNull { it.cislo == cislo }?.text
    return if (text == null) cislo.toString() else "$cislo) $text"
}
