package com.tallerwebi.dominio;

import com.tallerwebi.dominio.enums.DiaSemana;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ItemRutina {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  private Usuario socio;

  @ManyToOne
  private Usuario entrenador;

  @Enumerated(EnumType.STRING)
  private DiaSemana dia;

  private String nombreEjercicio;
  private String slugEjercicio;

  @Embedded
  private DetalleEjercicio detalle = new DetalleEjercicio();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Usuario getSocio() {
    return socio;
  }

  public void setSocio(Usuario socio) {
    this.socio = socio;
  }

  public Usuario getEntrenador() {
    return entrenador;
  }

  public void setEntrenador(Usuario entrenador) {
    this.entrenador = entrenador;
  }

  public DiaSemana getDia() {
    return dia;
  }

  public void setDia(DiaSemana dia) {
    this.dia = dia;
  }

  public String getNombreEjercicio() {
    return nombreEjercicio;
  }

  public void setNombreEjercicio(String nombreEjercicio) {
    this.nombreEjercicio = nombreEjercicio;
  }

  public String getSlugEjercicio() {
    return slugEjercicio;
  }

  public void setSlugEjercicio(String slugEjercicio) {
    this.slugEjercicio = slugEjercicio;
  }

  public DetalleEjercicio getDetalle() {
    return detalle;
  }

  public void setDetalle(DetalleEjercicio detalle) {
    this.detalle = detalle;
  }
}
