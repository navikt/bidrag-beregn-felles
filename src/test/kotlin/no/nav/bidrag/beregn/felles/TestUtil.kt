package no.nav.bidrag.beregn.felles

import no.nav.bidrag.beregn.felles.bo.Periode
import no.nav.bidrag.beregn.felles.bo.Sjablon
import no.nav.bidrag.beregn.felles.bo.SjablonInnhold
import no.nav.bidrag.beregn.felles.bo.SjablonNokkel
import no.nav.bidrag.beregn.felles.bo.SjablonPeriode
import no.nav.bidrag.beregn.felles.inntekt.InntektPeriodeGrunnlagUtenInntektType
import no.nav.bidrag.domene.enums.InntektType
import no.nav.bidrag.domene.enums.sjablon.SjablonInnholdNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonNokkelNavn
import no.nav.bidrag.domene.enums.sjablon.SjablonTallNavn
import java.math.BigDecimal
import java.time.LocalDate

object TestUtil {
    @JvmStatic
    fun byggSjabloner(): List<Sjablon> {
        val sjablonListe = mutableListOf<Sjablon>()

        // Barnetilsyn
        sjablonListe.add(
            Sjablon(
                SjablonNavn.BARNETILSYN.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.STONAD_TYPE.navn, "64"),
                    SjablonNokkel(SjablonNokkelNavn.TILSYN_TYPE.navn, "DO")
                ),
                listOf(SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELOP.navn, BigDecimal.valueOf(355)))
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.BARNETILSYN.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.STONAD_TYPE.navn, "64"),
                    SjablonNokkel(SjablonNokkelNavn.TILSYN_TYPE.navn, "DU")
                ),
                listOf(SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELOP.navn, BigDecimal.valueOf(258)))
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.BARNETILSYN.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.STONAD_TYPE.navn, "64"),
                    SjablonNokkel(SjablonNokkelNavn.TILSYN_TYPE.navn, "HO")
                ),
                listOf(SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELOP.navn, BigDecimal.valueOf(579)))
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.BARNETILSYN.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.STONAD_TYPE.navn, "64"),
                    SjablonNokkel(SjablonNokkelNavn.TILSYN_TYPE.navn, "HU")
                ),
                listOf(SjablonInnhold(SjablonInnholdNavn.BARNETILSYN_BELOP.navn, BigDecimal.valueOf(644)))
            )
        )

        // Bidragsevne
        sjablonListe
            .add(
                Sjablon(
                    SjablonNavn.BIDRAGSEVNE.navn,
                    listOf(SjablonNokkel(SjablonNokkelNavn.BOSTATUS.navn, "EN")),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.BOUTGIFT_BELOP.navn, BigDecimal.valueOf(9591)),
                        SjablonInnhold(SjablonInnholdNavn.UNDERHOLD_BELOP.navn, BigDecimal.valueOf(8925))
                    )
                )
            )
        sjablonListe
            .add(
                Sjablon(
                    SjablonNavn.BIDRAGSEVNE.navn,
                    listOf(SjablonNokkel(SjablonNokkelNavn.BOSTATUS.navn, "GS")),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.BOUTGIFT_BELOP.navn, BigDecimal.valueOf(5875)),
                        SjablonInnhold(SjablonInnholdNavn.UNDERHOLD_BELOP.navn, BigDecimal.valueOf(7557))
                    )
                )
            )

        // Forbruksutgifter
        sjablonListe
            .add(
                Sjablon(
                    SjablonNavn.FORBRUKSUTGIFTER.navn,
                    listOf(SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "18")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.navn, BigDecimal.valueOf(6985)))
                )
            )
        sjablonListe
            .add(
                Sjablon(
                    SjablonNavn.FORBRUKSUTGIFTER.navn,
                    listOf(SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "5")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.navn, BigDecimal.valueOf(3661)))
                )
            )
        sjablonListe
            .add(
                Sjablon(
                    SjablonNavn.FORBRUKSUTGIFTER.navn,
                    listOf(SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "99")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.navn, BigDecimal.valueOf(6985)))
                )
            )
        sjablonListe
            .add(
                Sjablon(
                    SjablonNavn.FORBRUKSUTGIFTER.navn,
                    listOf(SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "10")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.navn, BigDecimal.valueOf(5113)))
                )
            )
        sjablonListe
            .add(
                Sjablon(
                    SjablonNavn.FORBRUKSUTGIFTER.navn,
                    listOf(SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "14")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.FORBRUK_TOTAL_BELOP.navn, BigDecimal.valueOf(6099)))
                )
            )

        // Maks fradrag
        sjablonListe
            .add(
                Sjablon(
                    SjablonNavn.MAKS_FRADRAG.navn,
                    listOf(SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.navn, "1")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.navn, BigDecimal.valueOf(2083.33)))
                )
            )
        sjablonListe
            .add(
                Sjablon(
                    SjablonNavn.MAKS_FRADRAG.navn,
                    listOf(SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.navn, "2")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.navn, BigDecimal.valueOf(3333)))
                )
            )
        sjablonListe
            .add(
                Sjablon(
                    SjablonNavn.MAKS_FRADRAG.navn,
                    listOf(SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.navn, "3")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.navn, BigDecimal.valueOf(4583)))
                )
            )
        sjablonListe
            .add(
                Sjablon(
                    SjablonNavn.MAKS_FRADRAG.navn,
                    listOf(SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.navn, "4")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.navn, BigDecimal.valueOf(5833)))
                )
            )
        sjablonListe
            .add(
                Sjablon(
                    SjablonNavn.MAKS_FRADRAG.navn,
                    listOf(SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.navn, "5")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.navn, BigDecimal.valueOf(7083)))
                )
            )
        sjablonListe
            .add(
                Sjablon(
                    SjablonNavn.MAKS_FRADRAG.navn,
                    listOf(SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.navn, "6")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.navn, BigDecimal.valueOf(8333)))
                )
            )
        sjablonListe
            .add(
                Sjablon(
                    SjablonNavn.MAKS_FRADRAG.navn,
                    listOf(SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.navn, "7")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.navn, BigDecimal.valueOf(9583)))
                )
            )
        sjablonListe
            .add(
                Sjablon(
                    SjablonNavn.MAKS_FRADRAG.navn,
                    listOf(SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.navn, "8")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.navn, BigDecimal.valueOf(10833)))
                )
            )
        sjablonListe
            .add(
                Sjablon(
                    SjablonNavn.MAKS_FRADRAG.navn,
                    listOf(SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.navn, "99")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_FRADRAG_BELOP.navn, BigDecimal.valueOf(12083)))
                )
            )

        // Maks tilsyn
        sjablonListe
            .add(
                Sjablon(
                    SjablonNavn.MAKS_TILSYN.navn,
                    listOf(SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.navn, "1")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_TILSYN_BELOP.navn, BigDecimal.valueOf(6214)))
                )
            )
        sjablonListe
            .add(
                Sjablon(
                    SjablonNavn.MAKS_TILSYN.navn,
                    listOf(SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.navn, "2")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_TILSYN_BELOP.navn, BigDecimal.valueOf(8109)))
                )
            )
        sjablonListe
            .add(
                Sjablon(
                    SjablonNavn.MAKS_TILSYN.navn,
                    listOf(SjablonNokkel(SjablonNokkelNavn.ANTALL_BARN_TOM.navn, "99")),
                    listOf(SjablonInnhold(SjablonInnholdNavn.MAKS_TILSYN_BELOP.navn, BigDecimal.valueOf(9189)))
                )
            )

        // Samvaersfradrag
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "00"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "99")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.valueOf(1)),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(1)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.ZERO)
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "01"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "5")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.valueOf(3)),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(3)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(219))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "01"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "10")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.valueOf(3)),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(3)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(318))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "01"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "14")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.valueOf(3)),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(3)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(400))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "01"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "18")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.valueOf(3)),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(3)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(460))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "01"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "99")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.valueOf(3)),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(3)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(460))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "02"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "5")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(8)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(727))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "02"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "10")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(8)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(1052))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "02"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "14")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(8)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(1323))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "02"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "18")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(8)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(1525))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "02"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "99")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(8)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(1525))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "03"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "5")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(13)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(2082))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "03"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "10")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(13)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(2536))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "03"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "14")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(13)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(2914))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "03"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "18")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(13)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(3196))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "03"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "99")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(13)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(3196))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "04"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "5")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(15)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(2614))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "04"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "10")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(15)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(3184))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "04"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "14")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(15)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(3658))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "04"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "18")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(15)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(4012))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.SAMVAERSFRADRAG.navn,
                listOf(
                    SjablonNokkel(SjablonNokkelNavn.SAMVAERSKLASSE.navn, "04"),
                    SjablonNokkel(SjablonNokkelNavn.ALDER_TOM.navn, "99")
                ),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_DAGER_TOM.navn, BigDecimal.ZERO),
                    SjablonInnhold(SjablonInnholdNavn.ANTALL_NETTER_TOM.navn, BigDecimal.valueOf(15)),
                    SjablonInnhold(SjablonInnholdNavn.FRADRAG_BELOP.navn, BigDecimal.valueOf(4012))
                )
            )
        )

        // Sjablontall
        sjablonListe.add(
            Sjablon(
                SjablonTallNavn.ORDINAER_BARNETRYGD_BELOP.navn,
                emptyList(),
                listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(1054)))
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonTallNavn.ORDINAER_SMAABARNSTILLEGG_BELOP.navn,
                emptyList(),
                listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.ZERO))
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonTallNavn.BOUTGIFTER_BIDRAGSBARN_BELOP.navn,
                emptyList(),
                listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(2775)))
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonTallNavn.MINSTEFRADRAG_INNTEKT_PROSENT.navn,
                emptyList(),
                listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(31)))
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonTallNavn.MINSTEFRADRAG_INNTEKT_BELOP.navn,
                emptyList(),
                listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(85050)))
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonTallNavn.PERSONFRADRAG_KLASSE1_BELOP.navn,
                emptyList(),
                listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(56550)))
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonTallNavn.PERSONFRADRAG_KLASSE2_BELOP.navn,
                emptyList(),
                listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(56550)))
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonTallNavn.SKATTESATS_ALMINNELIG_INNTEKT_PROSENT.navn,
                emptyList(),
                listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(22)))
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonTallNavn.TRYGDEAVGIFT_PROSENT.navn,
                emptyList(),
                listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(8.2)))
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonTallNavn.FORDEL_SAERFRADRAG_BELOP.navn,
                emptyList(),
                listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(12977)))
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELOP.navn,
                emptyList(),
                listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.ZERO))
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonTallNavn.UNDERHOLD_EGNE_BARN_I_HUSSTAND_BELOP.navn,
                emptyList(),
                listOf(SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(3487)))
            )
        )

        // Trinnvis skattesats
        sjablonListe.add(
            Sjablon(
                SjablonNavn.TRINNVIS_SKATTESATS.navn,
                emptyList(),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.navn, BigDecimal.valueOf(964800)),
                    SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.navn, BigDecimal.valueOf(16.2))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.TRINNVIS_SKATTESATS.navn,
                emptyList(),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.navn, BigDecimal.valueOf(245650)),
                    SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.navn, BigDecimal.valueOf(4.2))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.TRINNVIS_SKATTESATS.navn,
                emptyList(),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.navn, BigDecimal.valueOf(617500)),
                    SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.navn, BigDecimal.valueOf(13.2))
                )
            )
        )
        sjablonListe.add(
            Sjablon(
                SjablonNavn.TRINNVIS_SKATTESATS.navn,
                emptyList(),
                listOf(
                    SjablonInnhold(SjablonInnholdNavn.INNTEKTSGRENSE_BELOP.navn, BigDecimal.valueOf(174500)),
                    SjablonInnhold(SjablonInnholdNavn.SKATTESATS_PROSENT.navn, BigDecimal.valueOf(1.9))
                )
            )
        )
        return sjablonListe
    }

//    @JvmStatic
//    fun byggInntektGrunnlagListeMedLikDatoFomLikGruppe(): List<InntektPeriodeGrunnlag> {
//        val inntektGrunnlagListe = mutableListOf<InntektPeriodeGrunnlag>()
//        inntektGrunnlagListe.add(
//            InntektPeriodeGrunnlag(
//                "REF1",
//                Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("9999-12-31")),
//                InntektType.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER,
//                BigDecimal.valueOf(200000),
//                false,
//                false
//            )
//        )
//        inntektGrunnlagListe.add(
//            InntektPeriodeGrunnlag(
//                "REF2",
//                Periode(LocalDate.parse("2018-06-01"), LocalDate.parse("2018-12-31")),
//                InntektType.ATTFORING_AAP,
//                BigDecimal.valueOf(150000),
//                false,
//                false
//            )
//        )
//        inntektGrunnlagListe.add(
//            InntektPeriodeGrunnlag(
//                "REF3",
//                Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")),
//                InntektType.ALOYSE,
//                BigDecimal.valueOf(250000),
//                false,
//                false
//            )
//        )
//        return inntektGrunnlagListe
//    }

//    @JvmStatic
//    fun byggInntektGrunnlagListeMedLikDatoFomUlikGruppe(): List<InntektPeriodeGrunnlag> {
//        val inntektGrunnlagListe = mutableListOf<InntektPeriodeGrunnlag>()
//        inntektGrunnlagListe.add(
//            InntektPeriodeGrunnlag(
//                "REF1",
//                Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("9999-12-31")),
//                InntektType.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER,
//                BigDecimal.valueOf(200000),
//                false,
//                false
//            )
//        )
//        inntektGrunnlagListe.add(
//            InntektPeriodeGrunnlag(
//                "REF2",
//                Periode(LocalDate.parse("2018-06-01"), LocalDate.parse("2018-12-31")),
//                InntektType.ATTFORING_AAP,
//                BigDecimal.valueOf(150000),
//                false,
//                false
//            )
//        )
//        inntektGrunnlagListe.add(
//            InntektPeriodeGrunnlag(
//                "REF3",
//                Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")),
//                InntektType.KAPITALINNTEKT_SKE,
//                BigDecimal.valueOf(250000),
//                false,
//                false
//            )
//        )
//        return inntektGrunnlagListe
//    }

//    @JvmStatic
//    fun byggInntektGrunnlagListeMedLikDatoFomUtenGruppe(): List<InntektPeriodeGrunnlag> {
//        val inntektGrunnlagListe = mutableListOf<InntektPeriodeGrunnlag>()
//        inntektGrunnlagListe.add(
//            InntektPeriodeGrunnlag(
//                "REF1",
//                Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("9999-12-31")),
//                InntektType.BARNETRYGD_MANUELL_VURDERING,
//                BigDecimal.valueOf(200000),
//                false,
//                false
//            )
//        )
//        inntektGrunnlagListe.add(
//            InntektPeriodeGrunnlag(
//                "REF2",
//                Periode(LocalDate.parse("2018-06-01"), LocalDate.parse("2018-12-31")),
//                InntektType.ATTFORING_AAP,
//                BigDecimal.valueOf(150000),
//                false,
//                false
//            )
//        )
//        inntektGrunnlagListe.add(
//            InntektPeriodeGrunnlag(
//                "REF3",
//                Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-12-31")),
//                InntektType.OVERGANGSSTONAD,
//                BigDecimal.valueOf(250000),
//                false,
//                false
//            )
//        )
//        return inntektGrunnlagListe
//    }

//    @JvmStatic
//    fun byggInntektGrunnlagListeDelvisOverlappSammeGruppe(): List<InntektPeriodeGrunnlag> {
//        val inntektGrunnlagListe = mutableListOf<InntektPeriodeGrunnlag>()
//        inntektGrunnlagListe.add(
//            InntektPeriodeGrunnlag(
//                "REF1",
//                Periode(LocalDate.parse("2020-01-01"), LocalDate.MAX),
//                InntektType.ALOYSE,
//                BigDecimal.valueOf(250000),
//                false,
//                false
//            )
//        )
//        inntektGrunnlagListe.add(
//            InntektPeriodeGrunnlag(
//                "REF2",
//                Periode(LocalDate.parse("2018-06-01"), LocalDate.parse("2018-12-31")),
//                InntektType.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER,
//                BigDecimal.valueOf(150000),
//                false,
//                false
//            )
//        )
//        inntektGrunnlagListe.add(
//            InntektPeriodeGrunnlag(
//                "REF3",
//                Periode(LocalDate.parse("2019-01-01"), LocalDate.MAX),
//                InntektType.SAKSBEHANDLER_BEREGNET_INNTEKT,
//                BigDecimal.valueOf(300000),
//                false,
//                false
//            )
//        )
//        inntektGrunnlagListe.add(
//            InntektPeriodeGrunnlag(
//                "REF4",
//                Periode(LocalDate.parse("2019-01-01"), LocalDate.MAX),
//                InntektType.KAPITALINNTEKT_EGNE_OPPLYSNINGER,
//                BigDecimal.valueOf(100000),
//                false,
//                false
//            )
//        )
//        inntektGrunnlagListe.add(
//            InntektPeriodeGrunnlag(
//                "REF5",
//                Periode(LocalDate.parse("2018-01-01"), LocalDate.MAX),
//                InntektType.INNTEKTSOPPLYSNINGER_ARBEIDSGIVER,
//                BigDecimal.valueOf(200000),
//                false,
//                false
//            )
//        )
//        return inntektGrunnlagListe
//    }

    @JvmStatic
    fun byggInntektGrunnlagUtvidetBarnetrygdFull(): List<InntektPeriodeGrunnlagUtenInntektType> {
        val inntektGrunnlagListe = mutableListOf<InntektPeriodeGrunnlagUtenInntektType>()
        inntektGrunnlagListe.add(
            InntektPeriodeGrunnlagUtenInntektType(
                "REF1",
                Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2018-06-01")),
                InntektType.UTVIDET_BARNETRYGD.name,
                BigDecimal.valueOf(12000),
                false,
                false
            )
        )
        inntektGrunnlagListe.add(
            InntektPeriodeGrunnlagUtenInntektType(
                "REF2",
                Periode(LocalDate.parse("2018-12-31"), LocalDate.parse("2019-05-31")),
                InntektType.UTVIDET_BARNETRYGD.name,
                BigDecimal.valueOf(12000),
                false,
                false
            )
        )
        inntektGrunnlagListe.add(
            InntektPeriodeGrunnlagUtenInntektType(
                "REF3",
                Periode(LocalDate.parse("2019-06-01"), LocalDate.parse("2020-01-01")),
                InntektType.UTVIDET_BARNETRYGD.name,
                BigDecimal.valueOf(12000),
                true,
                false
            )
        )
        inntektGrunnlagListe.add(
            InntektPeriodeGrunnlagUtenInntektType(
                "REF4",
                Periode(LocalDate.parse("2020-04-01"), LocalDate.parse("2020-09-01")),
                InntektType.UTVIDET_BARNETRYGD.name,
                BigDecimal.valueOf(10000),
                false,
                false
            )
        )
        inntektGrunnlagListe.add(
            InntektPeriodeGrunnlagUtenInntektType(
                "REF5",
                Periode(LocalDate.parse("2020-09-01"), LocalDate.parse("2021-01-01")),
                InntektType.UTVIDET_BARNETRYGD.name,
                BigDecimal.valueOf(10000),
                true,
                false
            )
        )
        inntektGrunnlagListe.add(
            InntektPeriodeGrunnlagUtenInntektType(
                "REF6",
                Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("2020-01-01")),
                InntektType.LØNNSINNTEKT.name,
                BigDecimal.valueOf(90000),
                false,
                false
            )
        )
        inntektGrunnlagListe.add(
            InntektPeriodeGrunnlagUtenInntektType(
                "REF7",
                Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("2021-01-01")),
                InntektType.LØNNSINNTEKT.name,
                BigDecimal.valueOf(105000),
                false,
                false
            )
        )
        inntektGrunnlagListe.add(
            InntektPeriodeGrunnlagUtenInntektType(
                "REF8",
                Periode(LocalDate.parse("2019-04-01"), LocalDate.parse("2019-08-01")),
                InntektType.KAPITALINNTEKT.name,
                BigDecimal.valueOf(30000),
                false,
                false
            )
        )
        inntektGrunnlagListe.add(
            InntektPeriodeGrunnlagUtenInntektType(
                "REF9",
                Periode(LocalDate.parse("2020-04-01"), LocalDate.parse("2020-08-01")),
                InntektType.KAPITALINNTEKT.name,
                BigDecimal.valueOf(10000),
                false,
                false
            )
        )
        return inntektGrunnlagListe
    }

    @JvmStatic
    fun byggInntektGrunnlagUtvidetBarnetrygdOvergang(): List<InntektPeriodeGrunnlagUtenInntektType> {
        val inntektGrunnlagListe = mutableListOf<InntektPeriodeGrunnlagUtenInntektType>()
        inntektGrunnlagListe.add(
            InntektPeriodeGrunnlagUtenInntektType(
                "REF1",
                Periode(LocalDate.parse("2012-06-01"), LocalDate.parse("2013-06-01")),
                InntektType.UTVIDET_BARNETRYGD.name,
                BigDecimal.valueOf(12000),
                false,
                true
            )
        )
        inntektGrunnlagListe.add(
            InntektPeriodeGrunnlagUtenInntektType(
                "REF2",
                Periode(LocalDate.parse("2012-06-01"), LocalDate.parse("2013-06-01")),
                InntektType.LØNNSINNTEKT.name,
                BigDecimal.valueOf(120000),
                false,
                false
            )
        )
        return inntektGrunnlagListe
    }

    @JvmStatic
    fun byggSjablontallGrunnlagUtvidetBarnetrygdFull(): List<SjablonPeriode> {
        val sjablontallPeriodeListe = mutableListOf<SjablonPeriode>()

        // Sjablon 0004
        sjablontallPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2011-07-01"), LocalDate.parse("2012-07-01")),
                Sjablon(
                    SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELOP.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(7500))
                    )
                )
            )
        )
        sjablontallPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2012-07-01"), LocalDate.parse("2013-07-01")),
                Sjablon(
                    SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELOP.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(8500))
                    )
                )
            )
        )

        // Sjablon 0030
        sjablontallPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2019-01-01")),
                Sjablon(
                    SjablonTallNavn.OVRE_INNTEKTSGRENSE_IKKE_I_SKATTEPOSISJON_BELOP.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(99540))
                    )
                )
            )
        )
        sjablontallPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("2020-01-01")),
                Sjablon(
                    SjablonTallNavn.OVRE_INNTEKTSGRENSE_IKKE_I_SKATTEPOSISJON_BELOP.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(105000))
                    )
                )
            )
        )
        sjablontallPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonTallNavn.OVRE_INNTEKTSGRENSE_IKKE_I_SKATTEPOSISJON_BELOP.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(100000))
                    )
                )
            )
        )

        // Sjablon 0031
        sjablontallPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2018-01-01"), LocalDate.parse("2019-01-01")),
                Sjablon(
                    SjablonTallNavn.NEDRE_INNTEKTSGRENSE_FULL_SKATTEPOSISJON_BELOP.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(99540))
                    )
                )
            )
        )
        sjablontallPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("2020-01-01")),
                Sjablon(
                    SjablonTallNavn.NEDRE_INNTEKTSGRENSE_FULL_SKATTEPOSISJON_BELOP.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(105000))
                    )
                )
            )
        )
        sjablontallPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-10-01")),
                Sjablon(
                    SjablonTallNavn.NEDRE_INNTEKTSGRENSE_FULL_SKATTEPOSISJON_BELOP.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(110000))
                    )
                )
            )
        )
        sjablontallPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2020-10-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonTallNavn.NEDRE_INNTEKTSGRENSE_FULL_SKATTEPOSISJON_BELOP.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(100000))
                    )
                )
            )
        )

        // Sjablon 0039
        sjablontallPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2018-07-01"), LocalDate.parse("2019-01-01")),
                Sjablon(
                    SjablonTallNavn.FORDEL_SAERFRADRAG_BELOP.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(12500))
                    )
                )
            )
        )
        sjablontallPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2019-01-01"), LocalDate.parse("2020-07-01")),
                Sjablon(
                    SjablonTallNavn.FORDEL_SAERFRADRAG_BELOP.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(13000))
                    )
                )
            )
        )
        sjablontallPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2020-07-01"), LocalDate.parse("9999-12-31")),
                Sjablon(
                    SjablonTallNavn.FORDEL_SAERFRADRAG_BELOP.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(14000))
                    )
                )
            )
        )
        return sjablontallPeriodeListe
    }

    @JvmStatic
    fun byggSjablontallGrunnlagUtvidetBarnetrygdOvergang(): List<SjablonPeriode> {
        val sjablontallPeriodeListe = mutableListOf<SjablonPeriode>()

        // Sjablon 0004
        sjablontallPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2011-07-01"), LocalDate.parse("2012-07-01")),
                Sjablon(
                    SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELOP.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(7500))
                    )
                )
            )
        )
        sjablontallPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2012-07-01"), LocalDate.parse("2013-07-01")),
                Sjablon(
                    SjablonTallNavn.FORDEL_SKATTEKLASSE2_BELOP.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(8500))
                    )
                )
            )
        )

        // Sjablon 0030
        sjablontallPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2012-01-01"), LocalDate.parse("2014-01-01")),
                Sjablon(
                    SjablonTallNavn.OVRE_INNTEKTSGRENSE_IKKE_I_SKATTEPOSISJON_BELOP.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(100000))
                    )
                )
            )
        )

        // Sjablon 0031
        sjablontallPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2012-01-01"), LocalDate.parse("2014-01-01")),
                Sjablon(
                    SjablonTallNavn.NEDRE_INNTEKTSGRENSE_FULL_SKATTEPOSISJON_BELOP.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(100000))
                    )
                )
            )
        )

        // Sjablon 0039
        sjablontallPeriodeListe.add(
            SjablonPeriode(
                Periode(LocalDate.parse("2012-01-01"), LocalDate.parse("2014-01-01")),
                Sjablon(
                    SjablonTallNavn.FORDEL_SAERFRADRAG_BELOP.navn,
                    emptyList(),
                    listOf(
                        SjablonInnhold(SjablonInnholdNavn.SJABLON_VERDI.navn, BigDecimal.valueOf(12500))
                    )
                )
            )
        )
        return sjablontallPeriodeListe
    }
}
