package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tallerwebi.dominio.enums.TipoMembresia;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class TipoMembresiaTest {

  @Test
  public void deberiaVencerUnMesDespuesUnaMembresiaMensual() {
    LocalDate fechaInicio = LocalDate.of(2026, 3, 10);
    LocalDate vencimiento = TipoMembresia.MENSUAL.calcularVencimiento(fechaInicio);

    LocalDate fechaEsperada = LocalDate.of(2026, 4, 10);
    assertThat(vencimiento, equalTo(fechaEsperada));
  }

  @Test
  public void deberiaVencerTresMesesDespuesUnaMembresiaTrimestral() {
    LocalDate fechaInicio = LocalDate.of(2026, 3, 10);
    LocalDate vencimiento = TipoMembresia.TRIMESTRAL.calcularVencimiento(fechaInicio);

    LocalDate fechaEsperada = LocalDate.of(2026, 6, 10);
    assertThat(vencimiento, equalTo(fechaEsperada));
  }

  @Test
  public void deberiaVencerUnAnioDespuesUnaMembresiaAnual() {
    LocalDate fechaInicio = LocalDate.of(2026, 3, 10);
    LocalDate vencimiento = TipoMembresia.ANUAL.calcularVencimiento(fechaInicio);

    LocalDate fechaEsperada = LocalDate.of(2027, 3, 10);
    assertThat(vencimiento, equalTo(fechaEsperada));
  }

  @Test
  public void deberiaVencerElUltimoDiaDelMesSiElMesSiguienteEsMasCorto() {
    LocalDate fechaInicio = LocalDate.of(2026, 1, 31);
    LocalDate vencimiento = TipoMembresia.MENSUAL.calcularVencimiento(fechaInicio);

    assertThat(vencimiento, equalTo(LocalDate.of(2026, 2, 28)));
  }

  @Test
  public void deberiaTenerLaCantidadDeMesesCorrespondiente() {
    int meses = TipoMembresia.TRIMESTRAL.getMeses();

    assertThat(meses, equalTo(3));
  }
}
