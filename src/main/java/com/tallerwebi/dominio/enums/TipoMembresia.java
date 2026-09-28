package com.tallerwebi.dominio.enums;

import java.time.LocalDate;

public enum TipoMembresia {
  MENSUAL(1),
  TRIMESTRAL(3),
  ANUAL(12);

  private int meses;

  TipoMembresia(int meses) {
    this.meses = meses;
  }

  public int getMeses() {
    return this.meses;
  }

  public LocalDate calcularVencimiento(LocalDate fechaInicio) {
    return fechaInicio.plusMonths(meses);
  }
}
