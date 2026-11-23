/*
 * Fbox: doplňky forku nad Element X.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package cz.fbox.x.features.fbox.impl.dotaz

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FboxOtiskTest {
    @Test
    fun `otisk odpovídá SHA-256 ze standardu`() {
        // Testovací vektor z FIPS 180-2.
        assertThat(sha256Hex("abc")).isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")
    }

    @Test
    fun `otisk Matrix ID s diakritikou počítá z UTF-8`() {
        assertThat(sha256Hex("@git:example.org")).hasLength(64)
        assertThat(sha256Hex("@žluťoučký:example.org")).isNotEqualTo(sha256Hex("@zlutoucky:example.org"))
    }
}
