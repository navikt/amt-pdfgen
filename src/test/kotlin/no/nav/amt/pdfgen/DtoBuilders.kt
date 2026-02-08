package no.nav.amt.pdfgen

import no.nav.amt.lib.models.hendelse.HendelseDeltaker
import no.nav.amt.lib.models.journalforing.pdf.EndringDto
import no.nav.amt.lib.models.journalforing.pdf.EndringsvedtakPdfDto
import no.nav.amt.lib.models.journalforing.pdf.HovedvedtakPdfDto
import no.nav.amt.lib.models.journalforing.pdf.InnholdPdfDto
import no.nav.amt.pdfgen.TestUtils.fixedDate

object DtoBuilders {
    fun hovedvedtakDeltaker(
        innholdPdfDto: InnholdPdfDto? = null,
        bakgrunnsinfo: String = "Bakgrunnsinfo",
        deltakelsesmengde: String = "deltakelsesmengde",
    ) = HovedvedtakPdfDto.DeltakerDto(
        fornavn = "Ola",
        mellomnavn = null,
        etternavn = "Nordmann",
        personident = "12345678910",
        innhold = innholdPdfDto,
        bakgrunnsinformasjon = bakgrunnsinfo,
        deltakelsesmengdeTekst = deltakelsesmengde,
        adresseDelesMedArrangor = false,
    )

    fun endringsvedtak(
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

    fun defaultEndring() =
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
}
