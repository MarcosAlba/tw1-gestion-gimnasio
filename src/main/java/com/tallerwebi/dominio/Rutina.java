package com.tallerwebi.dominio;

import com.tallerwebi.dominio.enums.Deporte;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Rutina {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  private Usuario socio;

  @Enumerated(EnumType.STRING)
  private Deporte deporte;

  private LocalDateTime fechaCreacion;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
    name = "rutina_ejercicio",
    joinColumns = @JoinColumn(name = "rutina_id"),
    inverseJoinColumns = @JoinColumn(name = "ejercicio_id")
  )
  private List<Ejercicio> ejercicios = new ArrayList<>();

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

  public Deporte getDeporte() {
    return deporte;
  }

  public void setDeporte(Deporte deporte) {
    this.deporte = deporte;
  }

  public LocalDateTime getFechaCreacion() {
    return fechaCreacion;
  }

  public void setFechaCreacion(LocalDateTime fechaCreacion) {
    this.fechaCreacion = fechaCreacion;
  }

  public List<Ejercicio> getEjercicios() {
    return ejercicios;
  }

  public void setEjercicios(List<Ejercicio> ejercicios) {
    this.ejercicios = ejercicios;
  }
}
