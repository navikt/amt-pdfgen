package no.nav.amt.pdfgen

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import no.nav.amt.lib.models.hendelse.HendelseDeltaker
import no.nav.amt.lib.models.journalforing.pdf.EndringDto
import no.nav.amt.lib.models.journalforing.pdf.EndringsvedtakPdfDto
import no.nav.amt.lib.models.journalforing.pdf.ForslagDto
import no.nav.amt.pdfgen.TestUtils.assertSectionText
import no.nav.amt.pdfgen.TestUtils.fixedDate
import no.nav.amt.pdfgen.TestUtils.render
import no.nav.amt.pdfgen.TestUtils.renderSection
import no.nav.amt.pdfgen.TestUtils.sectionText
import no.nav.amt.pdfgen.TestUtils.toNorwegianShortDate
import org.jsoup.nodes.Document

class EndringsvedtakBddTest :
    BehaviorSpec({

        Given("et endringsvedtak og sub-template dette-er-et-vedtak") {
            When("vedtaket rendres") {
                val endringsvedtakPdfDto = baseDto()
                val doc = renderSection("dette-er-et-vedtak", endringsvedtakPdfDto)

                Then("vises tittel for vedtaket") {
                    val heading = doc.selectFirst("h2").shouldNotBeNull()
                    heading.text() shouldBe "Dette er et vedtak"
                }

                And("vises forklarende tekst for vedtaket") {
                    val body = doc.selectFirst("p").shouldNotBeNull()
                    body.text() shouldBe
                        "Dette er et vedtak etter arbeidsmarkedsloven § 12 og forskrift om arbeidsmarkedstiltak " +
                        "kapittel ${endringsvedtakPdfDto.deltakerliste.forskriftskapittel}."
                }
            }
        }

        Given("endringsvedtak template") {

            When("rendering document with minimal payload") {
                val doc = renderEndringsvedtak(baseDto())

                Then("skal alle faste seksjoner finnes i dokumentet") {
                    doc.selectFirst("header") shouldNotBe null
                    doc.selectFirst("main") shouldNotBe null

                    val mainTitle = doc.selectFirst("main h1").shouldNotBeNull()
                    mainTitle.text() shouldBe "Endring: Endring i tiltak"

                    doc.select("h2").any { it.text() == "Dette er et vedtak" } shouldBe true
                    doc.select("h2").any { it.text() == "Du har rett til å klage" } shouldBe true
                    doc.select("h2").any { it.text() == "Har du spørsmål?" } shouldBe true

                    doc.selectFirst("#footer") shouldNotBe null
                    doc.text() shouldContain "Side"

                    doc.text() shouldContain "Med vennlig hilsen"
                    doc.text() shouldContain "Nav Saksbehandler"
                }
            }
        }

        Given("oppstartstype") {
            forAll(
                row(
                    HendelseDeltaker.Deltakerliste.Oppstartstype.LOPENDE,
                    "Du ble meldt på arbeidsmarkedstiltaket",
                ),
                row(
                    HendelseDeltaker.Deltakerliste.Oppstartstype.FELLES,
                    "Du ble søkt inn på arbeidsmarkedstiltaket",
                ),
            ) { oppstart, forventetTekst ->

                When("oppstart er $oppstart") {
                    val doc = renderEndringsvedtak(baseDto(oppstart = oppstart))

                    Then("skal korrekt ingress vises") {
                        doc.text() shouldContain forventetTekst
                    }
                }
            }
        }

        Given("klagerett") {
            forAll(
                row(a = true),
                row(a = false),
            ) { klagerett ->

                When("klagerett er $klagerett") {
                    val doc = renderEndringsvedtak(baseDto(klagerett = klagerett))

                    Then("skal klageseksjon være ${if (klagerett) "synlig" else "skjult"}") {
                        val finnes =
                            doc.select("h2").any { it.text() == "Du har rett til å klage" }

                        finnes shouldBe klagerett
                    }
                }
            }
        }

        Given("EndreDeltakelsesmengde") {
            val baseEndring =
                EndringDto.EndreDeltakelsesmengde(
                    tittel = "Deltakelsesmengde er endret",
                    begrunnelseFraNav = null,
                    forslagFraArrangor = null,
                    gyldigFra = null,
                )

            val baseForslag =
                ForslagDto.EndreDeltakelsesmengde(
                    deltakelsesmengdeTekst = "50%",
                    begrunnelseFraArrangor = null,
                )

            forAll(
                row(baseEndring),
                row(baseEndring.copy(begrunnelseFraNav = "Begrunnelse")),
                row(baseEndring.copy(forslagFraArrangor = baseForslag)),
                row(baseEndring.copy(forslagFraArrangor = baseForslag.copy(begrunnelseFraArrangor = "Arrangor sin begrunnelse"))),
                row(baseEndring.copy(gyldigFra = fixedDate)),
            ) { endring ->

                When("dokumentet rendres med endring: $endring") {
                    val doc = renderEndringsvedtak(baseDto(endringer = listOf(endring)))

                    Then("skal vise endring korrekt") {
                        doc.sectionText() shouldContain endring.tittel

                        doc.sectionText().assertSectionText("Nav sin begrunnelse:", endring.begrunnelseFraNav)
                        doc.sectionText().assertSectionText("Endringen gjelder fra:", endring.gyldigFra)

                        if (endring.forslagFraArrangor != null) {
                            val forslag =
                                endring.forslagFraArrangor.shouldBeInstanceOf<ForslagDto.EndreDeltakelsesmengde>()

                            doc.sectionText() shouldContain "Forslaget sendt fra arrangør:"
                            doc.sectionText() shouldContain "Ny deltakelsesmengde: ${forslag.deltakelsesmengdeTekst}"
                            doc.sectionText().assertSectionText("Begrunnelse:", forslag.begrunnelseFraArrangor)
                        }
                    }
                }
            }
        }

        Given("EndreStartdato") {
            val baseEndring =
                EndringDto.EndreStartdato(
                    tittel = "Startdato er endret",
                    begrunnelseFraNav = null,
                    forslagFraArrangor = null,
                )

            val baseForslag = ForslagDto.EndreStartdato(startdato = null, begrunnelseFraArrangor = null)

            forAll(
                row(baseEndring),
                row(baseEndring.copy(begrunnelseFraNav = "Begrunnelse")),
                row(baseEndring.copy(forslagFraArrangor = baseForslag)),
                row(baseEndring.copy(forslagFraArrangor = baseForslag.copy(startdato = fixedDate))),
                row(baseEndring.copy(forslagFraArrangor = baseForslag.copy(begrunnelseFraArrangor = "Arrangor sin begrunnelse"))),
            ) { endring ->

                When("dokumentet rendres med endring: $endring") {
                    val doc = renderEndringsvedtak(baseDto(endringer = listOf(endring)))

                    Then("skal vise endring korrekt") {
                        doc.sectionText() shouldContain endring.tittel

                        doc.sectionText().assertSectionText("Nav sin begrunnelse:", endring.begrunnelseFraNav)

                        if (endring.forslagFraArrangor != null) {
                            val forslag =
                                endring.forslagFraArrangor.shouldBeInstanceOf<ForslagDto.EndreStartdato>()

                            doc.sectionText() shouldContain "Forslaget sendt fra arrangør:"
                            doc.sectionText().assertSectionText("Ny oppstartsdato:", forslag.startdato)
                            doc.sectionText().assertSectionText("Begrunnelse:", forslag.begrunnelseFraArrangor)
                        }
                    }
                }
            }
        }

        Given("EndreStartdatoOgVarighet") {
            val baseEndring =
                EndringDto.EndreStartdatoOgVarighet(
                    tittel = "Startdato og varighet er endret",
                    begrunnelseFraNav = null,
                    forslagFraArrangor = null,
                    sluttdato = "1.1.2023",
                )

            val baseForslag =
                ForslagDto.EndreStartdatoOgVarighet(
                    startdato = null,
                    sluttdato = fixedDate,
                    begrunnelseFraArrangor = null,
                )

            forAll(
                row(baseEndring),
                row(baseEndring.copy(begrunnelseFraNav = "Begrunnelse")),
                row(baseEndring.copy(forslagFraArrangor = baseForslag)),
                row(baseEndring.copy(forslagFraArrangor = baseForslag.copy(startdato = fixedDate))),
                row(baseEndring.copy(forslagFraArrangor = baseForslag.copy(begrunnelseFraArrangor = "Arrangor sin begrunnelse"))),
            ) { endring ->

                When("dokumentet rendres med endring: $endring") {
                    val doc = renderEndringsvedtak(baseDto(endringer = listOf(endring)))

                    Then("skal vise endring korrekt") {
                        doc.sectionText() shouldContain endring.tittel

                        doc.sectionText().assertSectionText("Nav sin begrunnelse:", endring.begrunnelseFraNav)

                        if (endring.forslagFraArrangor != null) {
                            val forslag =
                                endring.forslagFraArrangor.shouldBeInstanceOf<ForslagDto.EndreStartdatoOgVarighet>()

                            doc.sectionText() shouldContain "Forslaget sendt fra arrangør:"
                            doc.sectionText() shouldContain "Ny sluttdato: ${forslag.sluttdato.toNorwegianShortDate()}"

                            doc.sectionText().assertSectionText("Ny oppstartsdato:", forslag.startdato)
                            doc.sectionText().assertSectionText("Begrunnelse:", forslag.begrunnelseFraArrangor)
                        }
                    }
                }
            }
        }

        Given("EndreStartdatoOgVarighet2") {
            val baseEndring =
                EndringDto.EndreStartdatoOgVarighet(
                    tittel = "Startdato og varighet er endret",
                    begrunnelseFraNav = null,
                    forslagFraArrangor = null,
                    sluttdato = "1.1.2023",
                )

            val baseForslag =
                ForslagDto.EndreStartdatoOgVarighet(
                    startdato = null,
                    sluttdato = fixedDate,
                    begrunnelseFraArrangor = null,
                )

            forAll(
                row(baseEndring),
                row(baseEndring.copy(begrunnelseFraNav = "Begrunnelse")),
                row(baseEndring.copy(forslagFraArrangor = baseForslag)),
                row(baseEndring.copy(forslagFraArrangor = baseForslag.copy(startdato = fixedDate))),
                row(baseEndring.copy(forslagFraArrangor = baseForslag.copy(begrunnelseFraArrangor = "Arrangor sin begrunnelse"))),
            ) { endring ->

                When("dokumentet rendres med endring: $endring") {
                    val doc = renderEndringsvedtak(baseDto(endringer = listOf(endring)))

                    Then("skal vise endring korrekt") {
                        doc.sectionText() shouldContain endring.tittel

                        doc.sectionText().assertSectionText("Nav sin begrunnelse:", endring.begrunnelseFraNav)

                        if (endring.forslagFraArrangor != null) {
                            val forslag =
                                endring.forslagFraArrangor.shouldBeInstanceOf<ForslagDto.EndreStartdatoOgVarighet>()

                            doc.sectionText() shouldContain "Forslaget sendt fra arrangør:"
                            doc.sectionText() shouldContain "Ny sluttdato: ${forslag.sluttdato.toNorwegianShortDate()}"

                            doc.sectionText().assertSectionText("Ny oppstartsdato:", forslag.startdato)
                            doc.sectionText().assertSectionText("Begrunnelse:", forslag.begrunnelseFraArrangor)
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
            oppstart: HendelseDeltaker.Deltakerliste.Oppstartstype?,
            klagerett: Boolean,
        ) = EndringsvedtakPdfDto.DeltakerlisteDto(
            navn = "Tiltaksliste",
            ledetekst = "Dette er ledeteksten",
            arrangor = EndringsvedtakPdfDto.ArrangorDto("Arrangør AS"),
            forskriftskapittel = 42,
            oppstart = oppstart,
            klagerett = klagerett,
        )

        private fun baseAvsender() =
            EndringsvedtakPdfDto.AvsenderDto(
                navn = "Nav Saksbehandler",
                enhet = "Nav Oslo",
            )

        private fun baseDto(
            endringer: List<EndringDto> = listOf(defaultEndring()),
            oppstart: HendelseDeltaker.Deltakerliste.Oppstartstype? = HendelseDeltaker.Deltakerliste.Oppstartstype.LOPENDE,
            klagerett: Boolean = true,
        ) = EndringsvedtakPdfDto(
            deltaker = baseDeltaker(),
            deltakerliste = baseDeltakerliste(oppstart, klagerett),
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
