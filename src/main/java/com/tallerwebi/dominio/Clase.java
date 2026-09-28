package com.tallerwebi.dominio;

import com.tallerwebi.dominio.enums.CapacidadFisica;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class Clase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String nombre;
  private LocalDateTime inicio;
  private Integer duracion;
  private String lugar;
  private Integer cupo;

  @Enumerated(EnumType.STRING)
  private CapacidadFisica capacidad;

  @ManyToOne
  private Usuario entrenador;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public LocalDateTime getInicio() {
    return inicio;
  }

  public void setInicio(LocalDateTime inicio) {
    this.inicio = inicio;
  }

  public Integer getDuracion() {
    return duracion;
  }

  public void setDuracion(Integer duracion) {
    this.duracion = duracion;
  }

  public String getLugar() {
    return lugar;
  }

  public void setLugar(String lugar) {
    this.lugar = lugar;
  }

  public Integer getCupo() {
    return cupo;
  }

  public void setCupo(Integer cupo) {
    this.cupo = cupo;
  }

  public CapacidadFisica getCapacidad() {
    return capacidad;
  }

  public void setCapacidad(CapacidadFisica capacidad) {
    this.capacidad = capacidad;
  }

  public Usuario getEntrenador() {
    return entrenador;
  }

  public void setEntrenador(Usuario entrenador) {
    this.entrenador = entrenador;
  }
}
