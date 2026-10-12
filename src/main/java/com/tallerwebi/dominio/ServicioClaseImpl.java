package com.tallerwebi.dominio;

import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.excepcion.ClaseSuperpuesta;
import com.tallerwebi.dominio.excepcion.EntrenadorInvalido;
import com.tallerwebi.dominio.excepcion.FechaClaseInvalida;
import com.tallerwebi.dominio.excepcion.LugarOcupado;
import com.tallerwebi.dominio.interfaces.RepositorioClase;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioClase;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioClase")
@Transactional
public class ServicioClaseImpl implements ServicioClase {

  private static final int DIAS_DE_LA_SEMANA = 7;

  private RepositorioClase repoClase;
  private RepositorioUsuario repoUsuario;

  @Autowired
  public ServicioClaseImpl(RepositorioClase repoClase, RepositorioUsuario repoUsuario) {
    this.repoClase = repoClase;
    this.repoUsuario = repoUsuario;
  }

  @Override
  public void crear(Clase clase, Long idEntrenador)
    throws EntrenadorInvalido, FechaClaseInvalida, ClaseSuperpuesta, LugarOcupado {
    Usuario entrenador = repoUsuario.buscarPorId(idEntrenador);
    if (entrenador == null || !"ENTRENADOR".equalsIgnoreCase(entrenador.getRol())) {
      throw new EntrenadorInvalido();
    }

    // Regla 1: No se pueden agendar clases en el pasado
    if (clase.getInicio() == null || clase.getInicio().isBefore(LocalDateTime.now())) {
      throw new FechaClaseInvalida();
    }

    // Regla 2: El entrenador no puede superponerse
    validarDisponibilidadEntrenador(clase, idEntrenador);

    // Regla 3: El lugar no puede estar ocupado en ese horario
    validarDisponibilidadLugar(clase);

    clase.setEntrenador(entrenador);
    repoClase.guardar(clase);
  }

  private void validarDisponibilidadEntrenador(Clase clase, Long idEntrenador)
    throws ClaseSuperpuesta {
    LocalDate dia = clase.getInicio().toLocalDate();
    List<Clase> clasesEntrenador = repoClase.buscarPorEntrenadorYFecha(idEntrenador, dia);

    if (haySolapamiento(clase, clasesEntrenador)) {
      throw new ClaseSuperpuesta();
    }
  }

  private void validarDisponibilidadLugar(Clase clase) throws LugarOcupado {
    LocalDate dia = clase.getInicio().toLocalDate();
    List<Clase> clasesLugar = repoClase.buscarPorLugarYFecha(clase.getLugar(), dia);

    if (haySolapamiento(clase, clasesLugar)) {
      throw new LugarOcupado();
    }
  }

  private boolean haySolapamiento(Clase nuevaClase, List<Clase> clasesExistentes) {
    LocalDateTime inicioNuevo = nuevaClase.getInicio();
    LocalDateTime finNuevo = inicioNuevo.plusMinutes(nuevaClase.getDuracion());

    for (Clase existente : clasesExistentes) {
      LocalDateTime inicioExistente = existente.getInicio();
      LocalDateTime finExistente = inicioExistente.plusMinutes(existente.getDuracion());

      // Se tocan si el nuevo arranca antes que termine el otro Y termina después que arranca el otro
      if (inicioNuevo.isBefore(finExistente) && finNuevo.isAfter(inicioExistente)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<Clase> listarProximas() {
    return repoClase.buscarDesde(LocalDateTime.now());
  }

  @Override
  public Map<LocalDate, List<Clase>> listarSemana(CapacidadFisica capacidad) {
    LocalDate hoy = LocalDate.now();

    // Un día por columna, en orden de fecha y con la lista vacía si no hay clases
    Map<LocalDate, List<Clase>> semana = new LinkedHashMap<>();
    for (int i = 0; i < DIAS_DE_LA_SEMANA; i++) {
      semana.put(hoy.plusDays(i), new ArrayList<>());
    }

    for (Clase clase : repoClase.buscarDesde(hoy.atStartOfDay())) {
      boolean coincideConElFiltro = capacidad == null || clase.getCapacidad() == capacidad;
      List<Clase> delDia = semana.get(clase.getInicio().toLocalDate());
      if (coincideConElFiltro && delDia != null) {
        delDia.add(clase);
      }
    }

    for (List<Clase> delDia : semana.values()) {
      delDia.sort(Comparator.comparing(Clase::getInicio));
    }
    return semana;
  }
}
