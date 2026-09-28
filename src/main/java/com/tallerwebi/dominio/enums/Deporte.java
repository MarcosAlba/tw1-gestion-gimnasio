package com.tallerwebi.dominio.enums;

public enum Deporte {
  TENIS(3, 2, 2, 1);

  private int agilidad;
  private int cardio;
  private int coordinacion;
  private int fuerza;

  Deporte(int agilidad, int cardio, int coordinacion, int fuerza) {
    this.agilidad = agilidad;
    this.cardio = cardio;
    this.coordinacion = coordinacion;
    this.fuerza = fuerza;
  }

  public int cantidadPara(CapacidadFisica capacidad) {
    return switch (capacidad) {
      case AGILIDAD -> agilidad;
      case CARDIO -> cardio;
      case COORDINACION -> coordinacion;
      case FUERZA -> fuerza;
    };
  }
}
