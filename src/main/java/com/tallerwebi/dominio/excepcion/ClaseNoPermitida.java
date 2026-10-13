package com.tallerwebi.dominio.excepcion;

public class ClaseNoPermitida extends Exception {

  private static final long serialVersionUID = 1L;

  public ClaseNoPermitida() {
    super("No tenés permiso para ver los inscriptos de una clase que no es tuya");
  }
}
