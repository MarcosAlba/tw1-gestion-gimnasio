package com.tallerwebi.dominio;

import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.enums.Deporte;
import com.tallerwebi.dominio.excepcion.SocioSinDeporte;
import com.tallerwebi.dominio.interfaces.RepositorioEjercicio;
import com.tallerwebi.dominio.interfaces.RepositorioRutina;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioRutina;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioRutina")
@Transactional
public class ServicioRutinaImpl implements ServicioRutina {

  private RepositorioRutina repoRutina;
  private RepositorioEjercicio repoEjercicio;
  private RepositorioUsuario repoUsuario;

  @Autowired
  public ServicioRutinaImpl(
    RepositorioRutina repoRutina,
    RepositorioEjercicio repoEjercicio,
    RepositorioUsuario reposUsuario
  ) {
    this.repoRutina = repoRutina;
    this.repoEjercicio = repoEjercicio;
    this.repoUsuario = reposUsuario;
  }

  @Override
  public Rutina generar(Long socioId) throws SocioSinDeporte {
    Usuario socio = repoUsuario.buscarPorId(socioId);
    if (socio == null || socio.getDeporte() == null) {
      throw new SocioSinDeporte();
    }

    Deporte deporte = socio.getDeporte();
    List<Ejercicio> ejercicios = new ArrayList<>();

    for (CapacidadFisica capacidad : CapacidadFisica.values()) {
      List<Ejercicio> disponibles = repoEjercicio.buscarPorCapacidad(capacidad);
      int cantidad = Math.min(deporte.cantidadPara(capacidad), disponibles.size()); // Math.min(a, b) elige el más chico, y así nunca pasás del tamaño de la lista.
      ejercicios.addAll(disponibles.subList(0, cantidad)); // subList(0, cantidad) devuelve los primeros cantidad elementos de la lista
    }

    Rutina rutina = new Rutina();
    rutina.setSocio(socio);
    rutina.setDeporte(deporte);
    rutina.setFechaCreacion(LocalDateTime.now());
    rutina.setEjercicios(ejercicios);
    repoRutina.guardar(rutina);
    return rutina;
  }

  @Override
  public Rutina obtenerUltima(Long socioId) {
    return repoRutina.buscarUltimaDeSocio(socioId);
  }
}
