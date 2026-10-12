package com.tallerwebi.dominio.excepcion;

public class LugarOcupado extends Exception {

  private static final long serialVersionUID = 1L;

  public LugarOcupado() {
    super("El lugar seleccionado ya está ocupado en ese horario");
  }
}
