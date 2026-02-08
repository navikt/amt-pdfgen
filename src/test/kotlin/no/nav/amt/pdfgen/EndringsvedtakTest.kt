package no.nav.amt.pdfgen

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import no.nav.amt.lib.models.deltakerliste.GjennomforingPameldingType
import no.nav.amt.lib.models.hendelse.HendelseDeltaker
import no.nav.amt.lib.models.journalforing.pdf.EndringDto
import no.nav.amt.lib.models.journalforing.pdf.EndringsvedtakPdfDto
import no.nav.amt.lib.models.journalforing.pdf.ForslagDto
import no.nav.amt.pdfgen.TestUtils.fixedDate
import no.nav.amt.pdfgen.TestUtils.render
import org.jsoup.nodes.Document

class EndringsvedtakTest :
    DescribeSpec({

        describe("Endringsvedtak PDF") {

            describe("Oppstart") {
                it("viser tekst for DIREKTE_VEDTAK oppstart") {
                    val doc = renderEndringsvedtak(baseDto(pameldingType = GjennomforingPameldingType.DIREKTE_VEDTAK))
                    doc.text() shouldContain "Du ble meldt på arbeidsmarkedstiltaket"
                }

                it("viser tekst for TRENGER_GODKJENNING oppstart") {
                    val doc =
                        renderEndringsvedtak(baseDto(pameldingType = GjennomforingPameldingType.TRENGER_GODKJENNING))
                    doc.text() shouldContain "Du ble søkt inn på arbeidsmarkedstiltaket"
                }
            }

            describe("Klagerett") {
                it("viser klagerett når true") {
                    val doc = renderEndringsvedtak(baseDto(klagerett = true))
                    doc.text() shouldContain "Du har rett til å klage"
                }

                it("viser unntak når klagerett er false") {
                    val doc = renderEndringsvedtak(baseDto(klagerett = false))
                    doc.text() shouldNotContain "Du har rett til å klage"
                }
            }

            describe("Endringstyper") {

                val endringer =
                    listOf(
                        defaultEndring(),
                        EndringDto.EndreStartdato(
                            begrunnelseFraNav = "Begrunnelse",
                            forslagFraArrangor = null,
                            tittel = "Startdato tittel",
                        ),
                        EndringDto.EndreSluttdato(
                            begrunnelseFraNav = "Begrunnelse",
                            forslagFraArrangor = null,
                            tittel = "Sluttdato tittel",
                        ),
                        EndringDto.ForlengDeltakelse(
                            begrunnelseFraNav = "Begrunnelse",
                            forslagFraArrangor = null,
                            tittel = "Tittel",
                        ),
                        EndringDto.IkkeAktuell(
                            aarsak = "Årsak",
                            begrunnelseFraNav = "BegrunnelseFraNav",
                            forslagFraArrangor = null,
                        ),
                        EndringDto.AvsluttDeltakelse(
                            aarsak = "Årsak",
                            begrunnelseFraNav = "BegrunnelseFraNav",
                            forslagFraArrangor = null,
                            harDeltatt = "Ja",
                            harFullfort = "Nei",
                            tittel = "Tittel",
                        ),
                        EndringDto.EndreAvslutning(
                            aarsak = "Årsak",
                            begrunnelseFraNav = "BegrunnelseFraNav",
                            forslagFraArrangor = null,
                            harFullfort = "Ja",
                            sluttdato = "2024-01-01",
                            tittel = "Tittel",
                        ),
                        EndringDto.AvbrytDeltakelse(
                            aarsak = "Årsak",
                            begrunnelseFraNav = "BegrunnelseFraNav",
                            forslagFraArrangor = null,
                            harDeltatt = "Ja",
                            harFullfort = "Nei",
                            tittel = "Tittel",
                        ),
                        EndringDto.EndreBakgrunnsinformasjon(
                            bakgrunnsinformasjon = "Bakgrunnstekst",
                        ),
                        EndringDto.EndreInnhold(
                            innhold = listOf("Punkt1", "Punkt2"),
                            innholdBeskrivelse = "Beskrivelse",
                        ),
                    )

                endringer.forEach { endring ->

                    describe(endring::class.simpleName!!) {
                        it("viser hovedtittel") {
                            val doc = renderEndringsvedtak(baseDto(listOf(endring)))
                            doc.selectFirst("h1")!!.text() shouldBe "Endring: Endring i tiltak"
                        }

                        // Nested ForslagDto hvis endringen har forslag
                        val forslagListe =
                            when (endring) {
                                is EndringDto.EndreDeltakelsesmengde -> {
                                    listOf(
                                        ForslagDto.EndreDeltakelsesmengde(
                                            deltakelsesmengdeTekst = "50%",
                                            begrunnelseFraArrangor = "Redusert deltakelse",
                                        ),
                                    )
                                }

                                is EndringDto.EndreStartdato -> {
                                    listOf(
                                        ForslagDto.EndreStartdato(
                                            startdato = fixedDate.plusDays(5),
                                            begrunnelseFraArrangor = "Ny start",
                                        ),
                                    )
                                }

                                is EndringDto.EndreSluttdato -> {
                                    listOf(
                                        ForslagDto.EndreSluttdato(
                                            sluttdato = fixedDate.plusMonths(1),
                                            begrunnelseFraArrangor = "Ny sluttdato",
                                        ),
                                    )
                                }

                                is EndringDto.ForlengDeltakelse -> {
                                    listOf(
                                        ForslagDto.ForlengDeltakelse(
                                            sluttdato = fixedDate.plusMonths(2),
                                            begrunnelseFraArrangor = "Forlenget",
                                        ),
                                    )
                                }

                                is EndringDto.AvsluttDeltakelse -> {
                                    listOf(
                                        ForslagDto.AvsluttDeltakelse(
                                            aarsak = "Sykdom",
                                            sluttdato = fixedDate.plusMonths(1),
                                            harDeltatt = "Ja",
                                            harFullfort = "Nei",
                                            begrunnelseFraArrangor = "Anbefalt avslutning",
                                        ),
                                    )
                                }

                                else -> {
                                    emptyList()
                                }
                            }

                        forslagListe.forEach { forslag ->
                            describe("Forslag: ${forslag::class.simpleName}") {
                                it("rendrer forslag korrekt") {
                                    val endringMedForslag =
                                        when (endring) {
                                            is EndringDto.EndreDeltakelsesmengde -> endring.copy(forslagFraArrangor = forslag)
                                            is EndringDto.EndreStartdato -> endring.copy(forslagFraArrangor = forslag)
                                            is EndringDto.EndreSluttdato -> endring.copy(forslagFraArrangor = forslag)
                                            is EndringDto.ForlengDeltakelse -> endring.copy(forslagFraArrangor = forslag)
                                            is EndringDto.AvsluttDeltakelse -> endring.copy(forslagFraArrangor = forslag)
                                            else -> endring
                                        }

                                    val doc = renderEndringsvedtak(baseDto(listOf(endringMedForslag)))

                                    // Sjekk generell tilstedeværelse
                                    doc.text() shouldContain "Forslaget sendt fra arrangør"

                                    // spesifikke felter per ForslagDto-type
                                    when (forslag) {
                                        is ForslagDto.EndreDeltakelsesmengde -> {
                                            doc.text() shouldContain forslag.deltakelsesmengdeTekst
                                            forslag.begrunnelseFraArrangor?.let { doc.text() shouldContain it }
                                        }

                                        is ForslagDto.EndreStartdato -> {
                                            forslag.begrunnelseFraArrangor?.let { doc.text() shouldContain it }
                                        }

                                        is ForslagDto.EndreSluttdato -> {
                                            forslag.begrunnelseFraArrangor?.let { doc.text() shouldContain it }
                                        }

                                        is ForslagDto.ForlengDeltakelse -> {
                                            forslag.begrunnelseFraArrangor?.let { doc.text() shouldContain it }
                                        }

                                        is ForslagDto.AvsluttDeltakelse -> {
                                            forslag.aarsak?.let { doc.text() shouldContain it }
                                            forslag.harDeltatt?.let { doc.text() shouldContain it }
                                            forslag.harFullfort?.let { doc.text() shouldContain it }
                                            forslag.begrunnelseFraArrangor?.let { doc.text() shouldContain it }
                                        }

                                        else -> {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }) {
    companion object {
        private fun renderEndringsvedtak(payload: EndringsvedtakPdfDto): Document = render("endringsvedtak", payload)

        private fun defaultEndring() =
            EndringDto.EndreDeltakelsesmengde(
                tittel = "Deltakelsesmengde er endret",
                begrunnelseFraNav = "Begrunnelse",
                forslagFraArrangor = null,
                gyldigFra = fixedDate,
            )

        private fun baseDeltaker() =
            EndringsvedtakPdfDto.DeltakerDto(
                fornavn = "Ola",
                mellomnavn = null,
                etternavn = "Nordmann",
                personident = "12345678910",
                opprettetDato = fixedDate,
            )

        private fun baseDeltakerliste(
            klagerett: Boolean,
            pameldingstype: GjennomforingPameldingType
        ) = EndringsvedtakPdfDto.DeltakerlisteDto(
            navn = "Tiltaksliste",
            ledetekst = "Dette er ledeteksten",
            arrangor = EndringsvedtakPdfDto.ArrangorDto("Arrangør AS"),
            forskriftskapittel = 42,
            harKlagerett = klagerett,
            pameldingstype = pameldingstype
        )

        private fun baseAvsender() =
            EndringsvedtakPdfDto.AvsenderDto(
                navn = "Nav Saksbehandler",
                enhet = "Nav Oslo",
            )

        private fun baseDto(
            endringer: List<EndringDto> = listOf(defaultEndring()),
            pameldingType: GjennomforingPameldingType = GjennomforingPameldingType.DIREKTE_VEDTAK,
            klagerett: Boolean = true,
        ) = EndringsvedtakPdfDto(
            deltaker = baseDeltaker(),
            deltakerliste = baseDeltakerliste(klagerett, pameldingType),
            endringer = endringer,
            avsender = baseAvsender(),
            vedtaksdato = fixedDate,
            forsteVedtakFattet = fixedDate.minusDays(10),
            sidetittel = "Endring i tiltak",
            ingressnavn = "Arbeidsforberedende trening",
            opprettetDato = fixedDate.minusMonths(1),
        )
    }
}
