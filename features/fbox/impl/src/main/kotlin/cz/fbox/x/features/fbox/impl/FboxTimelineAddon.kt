/*
 * Fbox: doplňky forku nad Element X.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package cz.fbox.x.features.fbox.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import cz.fbox.x.features.fbox.impl.di.FboxBindings
import cz.fbox.x.features.fbox.impl.dotaz.FboxDotazParser
import cz.fbox.x.features.fbox.impl.dotaz.FboxDotazView
import cz.fbox.x.features.fbox.impl.dotaz.FboxOdpovedStav
import io.element.android.libraries.architecture.bindings

/**
 * Jediný vstupní bod, který volá kód upstreamu (TimelineItemEventRow): doplněk vykreslený pod zprávou.
 *
 * Parametry jsou záměrně jen řetězce, aby náš modul nezávisel na typech z `features:messages:impl`
 * a změny jejich modelu časové osy se nás nedotkly.
 *
 * Pro naprostou většinu zpráv skončí na první podmínce a nevykreslí nic.
 *
 * @param sessionId Matrix ID přihlášeného uživatele.
 * @param senderId Matrix ID odesílatele zprávy.
 * @param eventId ID události; null u zprávy, která ještě neodešla.
 * @param body prostý text zprávy; null u zpráv, které nejsou textové.
 */
@Composable
fun FboxTimelineAddon(
    sessionId: String,
    senderId: String,
    eventId: String?,
    body: String?,
    modifier: Modifier = Modifier,
) {
    if (body == null || eventId == null || !FboxDotazParser.muzeBytDotaz(body)) return
    // V náhledech a testech snímků není graf závislostí k dispozici.
    if (LocalInspectionMode.current) return

    val context = LocalContext.current
    val controller = remember(context) {
        runCatching { context.bindings<FboxBindings>().fboxDotazController() }.getOrNull()
    } ?: return
    val dotaz = remember(senderId, body) { controller.rozpoznej(senderId, body) } ?: return

    val stavy by controller.stavyOdpovedi.collectAsState()
    FboxDotazView(
        dotaz = dotaz,
        stav = stavy[eventId] ?: FboxOdpovedStav.Zadna,
        onOdpoved = { cislo -> controller.odpovez(sessionId = sessionId, eventId = eventId, cislo = cislo) },
        modifier = modifier,
    )
}
