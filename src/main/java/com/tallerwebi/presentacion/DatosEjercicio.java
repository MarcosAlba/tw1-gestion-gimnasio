package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.enums.NivelDificultad;

public class DatosEjercicio {

  private String nombre;
  private String descripcion;
  private CapacidadFisica capacidad;
  private NivelDificultad dificultad;

  public DatosEjercicio() {}

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public CapacidadFisica getCapacidad() {
    return capacidad;
  }

  public void setCapacidad(CapacidadFisica capacidad) {
    this.capacidad = capacidad;
  }

  public NivelDificultad getDificultad() {
    return dificultad;
  }

  public void setDificultad(NivelDificultad dificultad) {
    this.dificultad = dificultad;
  }
}
