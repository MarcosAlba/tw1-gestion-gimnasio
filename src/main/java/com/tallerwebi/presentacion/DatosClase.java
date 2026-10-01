package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.enums.CapacidadFisica;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public class DatosClase {

  private String nombre;

  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
  private LocalDateTime inicio;

  private Integer duracion;
  private String lugar;
  private Integer cupo;
  private CapacidadFisica capacidad;

  public DatosClase() {}

  public CapacidadFisica getCapacidad() {
    return capacidad;
  }

  public void setCapacidad(CapacidadFisica capacidad) {
    this.capacidad = capacidad;
  }

  public Integer getCupo() {
    return cupo;
  }

  public void setCupo(Integer cupo) {
    this.cupo = cupo;
  }

  public String getLugar() {
    return lugar;
  }

  public void setLugar(String lugar) {
    this.lugar = lugar;
  }

  public Integer getDuracion() {
    return duracion;
  }

  public void setDuracion(Integer duracion) {
    this.duracion = duracion;
  }

  public LocalDateTime getInicio() {
    return inicio;
  }

  public void setInicio(LocalDateTime inicio) {
    this.inicio = inicio;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }
}
