package no.nav.bidrag.inntekt.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class VersionProviderTest {

    @Test
    fun `Skal hente versjon fra fil`() {
        VersionProvider.APP_VERSJON shouldBe "2024.01.23.3123213123_ORIGINAL_COMMIT_HASH"
    }
}
