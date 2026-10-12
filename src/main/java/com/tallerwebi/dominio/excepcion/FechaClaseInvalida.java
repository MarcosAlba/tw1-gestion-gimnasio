package com.tallerwebi.dominio.excepcion;

public class FechaClaseInvalida extends Exception {

  private static final long serialVersionUID = 1L;

  public FechaClaseInvalida() {
    super("La fecha y hora de la clase no puede ser anterior al momento actual");
  }
}
