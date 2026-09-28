package com.tallerwebi.dominio;

import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.enums.NivelDificultad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Ejercicio {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String nombre;

  private String descripcion;

  @Enumerated(EnumType.STRING)
  private CapacidadFisica capacidad;

  @Enumerated(EnumType.STRING)
  private NivelDificultad dificultad;

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
