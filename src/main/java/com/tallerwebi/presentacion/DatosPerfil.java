package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.enums.Deporte;
import org.springframework.web.multipart.MultipartFile;

// Solo lo que el usuario puede editar: el formulario nunca se enlaza directo a Usuario
public class DatosPerfil {

  private String nombre;
  private String apellido;
  private Integer edad;
  private Deporte deporte;
  private MultipartFile foto;

  public DatosPerfil() {}

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getApellido() {
    return apellido;
  }

  public void setApellido(String apellido) {
    this.apellido = apellido;
  }

  public Integer getEdad() {
    return edad;
  }

  public void setEdad(Integer edad) {
    this.edad = edad;
  }

  public Deporte getDeporte() {
    return deporte;
  }

  public void setDeporte(Deporte deporte) {
    this.deporte = deporte;
  }

  public MultipartFile getFoto() {
    return foto;
  }

  public void setFoto(MultipartFile foto) {
    this.foto = foto;
  }
}
