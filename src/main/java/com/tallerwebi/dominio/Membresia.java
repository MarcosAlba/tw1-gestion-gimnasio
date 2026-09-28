package com.tallerwebi.dominio;

import com.tallerwebi.dominio.enums.TipoMembresia;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;

@Entity
public class Membresia {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  private Usuario socio;

  @Enumerated(EnumType.STRING)
  private TipoMembresia tipo;

  private LocalDate fechaInicio;
  private LocalDate fechaVencimiento;

  public Membresia() {}

  public Membresia(Usuario socio, TipoMembresia tipo, LocalDate fechaInicio) {
    this.socio = socio;
    this.tipo = tipo;
    this.fechaInicio = fechaInicio;
    this.fechaVencimiento = tipo.calcularVencimiento(fechaInicio);
  }

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

  public TipoMembresia getTipo() {
    return tipo;
  }

  public void setTipo(TipoMembresia tipo) {
    this.tipo = tipo;
  }

  public LocalDate getFechaInicio() {
    return fechaInicio;
  }

  public void setFechaInicio(LocalDate fechaInicio) {
    this.fechaInicio = fechaInicio;
  }

  public LocalDate getFechaVencimiento() {
    return fechaVencimiento;
  }

  public void setFechaVencimiento(LocalDate fechaVencimiento) {
    this.fechaVencimiento = fechaVencimiento;
  }
}
