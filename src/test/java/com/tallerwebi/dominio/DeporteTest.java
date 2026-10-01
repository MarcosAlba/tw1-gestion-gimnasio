package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.enums.Deporte;
import org.junit.jupiter.api.Test;

public class DeporteTest {

  @Test
  public void deberiaHaberTresEjerciciosDeAgilidadEnRutinaDeTenis() {
    int cantidadAgilidad = Deporte.TENIS.cantidadPara(CapacidadFisica.AGILIDAD);

    assertThat(cantidadAgilidad, equalTo(3));
  }

  @Test
  public void deberiaHaberDosEjerciciosDeCardioEnRutinaDeTenis() {
    int cantidadCardio = Deporte.TENIS.cantidadPara(CapacidadFisica.CARDIO);

    assertThat(cantidadCardio, equalTo(2));
  }

  @Test
  public void deberiaHaberDosEjerciciosDeCoordinacionEnRutinaDeTenis() {
    int cantidadCoordinacion = Deporte.TENIS.cantidadPara(CapacidadFisica.COORDINACION);

    assertThat(cantidadCoordinacion, equalTo(2));
  }

  @Test
  public void deberiaHaberUnEjercicioDeFuerzaEnRutinaDeTenis() {
    int cantidadFuerza = Deporte.TENIS.cantidadPara(CapacidadFisica.FUERZA);

    assertThat(cantidadFuerza, equalTo(1));
  }
}
