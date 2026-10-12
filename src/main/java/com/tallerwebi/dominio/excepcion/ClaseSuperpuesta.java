package com.tallerwebi.dominio.excepcion;

public class ClaseSuperpuesta extends Exception {

  private static final long serialVersionUID = 1L;

  public ClaseSuperpuesta() {
    super("Ya tenés una clase asignada en ese rango horario");
  }
}
