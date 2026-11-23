/*
 * Fbox: doplňky forku nad Element X.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package cz.fbox.x.features.fbox.impl.dotaz

import cz.fbox.x.features.fbox.impl.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.services.appnavstate.api.ActiveRoomsHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Stav odpovědi na jeden dotaz (podle ID události se zprávou). */
sealed interface FboxOdpovedStav {
    data object Zadna : FboxOdpovedStav
    data class Odesila(val cislo: Int) : FboxOdpovedStav
    data class Odeslano(val cislo: Int) : FboxOdpovedStav
    data class Chyba(val cislo: Int, val zprava: String) : FboxOdpovedStav
}

/**
 * Rozpoznává dotazy dispečera a posílá na ně odpověď.
 *
 * Žije v rozsahu aplikace, aby se k němu dostala časová osa bez zásahu do jejích presenterů.
 * Odpověď jde do místnosti, která je právě otevřená ([ActiveRoomsHolder]), jako odpověď (reply) na zprávu s dotazem.
 *
 * Stav „odesláno“ je jen v paměti: po restartu aplikace se tlačítka u starého dotazu ukážou znovu.
 * Dispečer druhou odpověď na vyřízený dotaz ignoruje, takže to nevadí.
 */
@SingleIn(AppScope::class)
@Inject
class FboxDotazController(
    private val activeRoomsHolder: ActiveRoomsHolder,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
) {
    private val odesilatele: Set<String> = BuildConfig.DOTAZ_ODESILATELE
        .split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toSet()

    private val stavy = MutableStateFlow<Map<String, FboxOdpovedStav>>(emptyMap())
    val stavyOdpovedi: StateFlow<Map<String, FboxOdpovedStav>> = stavy.asStateFlow()

    /** Vrátí dotaz, jen když zprávu poslal povolený odesílatel a text má tvar dotazu. */
    fun rozpoznej(senderId: String, body: String): FboxDotaz? {
        if (senderId !in odesilatele) return null
        return FboxDotazParser.parse(body)
    }

    fun odpovez(sessionId: String, eventId: String, cislo: Int) {
        val aktualni = stavy.value[eventId]
        if (aktualni is FboxOdpovedStav.Odesila || aktualni is FboxOdpovedStav.Odeslano) return
        nastav(eventId, FboxOdpovedStav.Odesila(cislo))
        appCoroutineScope.launch {
            val room = activeRoomsHolder.getActiveRoom(SessionId(sessionId))
            if (room == null) {
                nastav(eventId, FboxOdpovedStav.Chyba(cislo, "Místnost není otevřená"))
                return@launch
            }
            room.liveTimeline
                .replyMessage(
                    repliedToEventId = EventId(eventId),
                    body = cislo.toString(),
                    htmlBody = null,
                    intentionalMentions = emptyList(),
                )
                .fold(
                    onSuccess = { nastav(eventId, FboxOdpovedStav.Odeslano(cislo)) },
                    onFailure = { nastav(eventId, FboxOdpovedStav.Chyba(cislo, it.message ?: it.javaClass.simpleName)) },
                )
        }
    }

    private fun nastav(eventId: String, stav: FboxOdpovedStav) {
        stavy.update { it + (eventId to stav) }
    }
}
