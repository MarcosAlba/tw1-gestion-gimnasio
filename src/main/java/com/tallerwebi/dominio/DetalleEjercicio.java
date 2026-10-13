package com.tallerwebi.dominio;

import jakarta.persistence.Embeddable;

@Embeddable
public class DetalleEjercicio {

  private int series;
  private int repeticiones;
  private Double pesoKg;
  private String observaciones;
  private boolean completado;
  private Double pesoLevantadoKg;

  public DetalleEjercicio() {
    // Constructor requerido por JPA
  }

  public DetalleEjercicio(int series, int repeticiones, Double pesoKg, String observaciones) {
    this.series = series;
    this.repeticiones = repeticiones;
    this.pesoKg = pesoKg;
    this.observaciones = observaciones;
    this.completado = false;
  }

  public int getSeries() {
    return series;
  }

  public void setSeries(int series) {
    this.series = series;
  }

  public int getRepeticiones() {
    return repeticiones;
  }

  public void setRepeticiones(int repeticiones) {
    this.repeticiones = repeticiones;
  }

  public Double getPesoKg() {
    return pesoKg;
  }

  public void setPesoKg(Double pesoKg) {
    this.pesoKg = pesoKg;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }

  public boolean isCompletado() {
    return completado;
  }

  public void setCompletado(boolean completado) {
    this.completado = completado;
  }

  public Double getPesoLevantadoKg() {
    return pesoLevantadoKg;
  }

  public void setPesoLevantadoKg(Double pesoLevantadoKg) {
    this.pesoLevantadoKg = pesoLevantadoKg;
  }
}
