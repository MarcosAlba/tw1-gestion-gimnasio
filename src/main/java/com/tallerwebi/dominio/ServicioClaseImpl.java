package com.tallerwebi.dominio;

import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.enums.EstadoReserva;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.dominio.interfaces.RepositorioClase;
import com.tallerwebi.dominio.interfaces.RepositorioReserva;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioClase;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioClase")
@Transactional
public class ServicioClaseImpl implements ServicioClase {

  private static final int DIAS_DE_LA_SEMANA = 7;

  private final RepositorioClase repositorioClase;
  private final RepositorioUsuario repositorioUsuario;
  private final RepositorioReserva repositorioReserva;

  @Autowired
  public ServicioClaseImpl(
    RepositorioClase repositorioClase,
    RepositorioUsuario repositorioUsuario,
    RepositorioReserva repositorioReserva
  ) {
    this.repositorioClase = repositorioClase;
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioReserva = repositorioReserva;
  }

  @Override
  public void crear(Clase clase, Long idEntrenador)
    throws EntrenadorInvalido, FechaClaseInvalida, ClaseSuperpuesta, LugarOcupado {
    Usuario entrenador = repositorioUsuario.buscarPorId(idEntrenador);
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
    repositorioClase.guardar(clase);
  }

  private void validarDisponibilidadEntrenador(Clase clase, Long idEntrenador)
    throws ClaseSuperpuesta {
    LocalDate dia = clase.getInicio().toLocalDate();
    List<Clase> clasesEntrenador = repositorioClase.buscarPorEntrenadorYFecha(idEntrenador, dia);

    if (haySolapamiento(clase, clasesEntrenador)) {
      throw new ClaseSuperpuesta();
    }
  }

  private void validarDisponibilidadLugar(Clase clase) throws LugarOcupado {
    LocalDate dia = clase.getInicio().toLocalDate();
    List<Clase> clasesLugar = repositorioClase.buscarPorLugarYFecha(clase.getLugar(), dia);

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
    return repositorioClase.buscarDesde(LocalDateTime.now());
  }

  @Override
  public Map<LocalDate, List<Clase>> listarSemana(CapacidadFisica capacidad, String filtroTexto) {
    LocalDate hoy = LocalDate.now();
    LocalDateTime inicioSemana = hoy.atStartOfDay();
    List<Clase> clases = repositorioClase.buscarDesde(inicioSemana);

    Map<LocalDate, List<Clase>> semana = new LinkedHashMap<>();
    for (int i = 0; i < 7; i++) {
      semana.put(hoy.plusDays(i), new ArrayList<>());
    }

    String texto = (filtroTexto != null) ? filtroTexto.trim().toLowerCase(Locale.ROOT) : "";

    for (Clase clase : clases) {
      LocalDate fechaClase = clase.getInicio().toLocalDate();
      if (semana.containsKey(fechaClase)) {
        boolean coincideCapacidad = (capacidad == null || clase.getCapacidad() == capacidad);

        boolean coincideTexto =
          texto.isEmpty() ||
          (clase.getLugar() != null && clase.getLugar().toLowerCase(Locale.ROOT).contains(texto)) ||
          (clase.getEntrenador() != null &&
            clase.getEntrenador().getNombre() != null &&
            clase.getEntrenador().getNombre().toLowerCase(Locale.ROOT).contains(texto));

        if (coincideCapacidad && coincideTexto) {
          semana.get(fechaClase).add(clase);
        }
      }
    }

    // Ordenar turnos por hora de inicio
    for (List<Clase> listaTurnos : semana.values()) {
      listaTurnos.sort((c1, c2) -> c1.getInicio().compareTo(c2.getInicio()));
    }

    return semana;
  }

  @Override
  public Clase obtenerPorId(Long idClase) throws ClaseNoEncontrada {
    Clase clase = repositorioClase.buscarPorId(idClase);
    if (clase == null) {
      throw new ClaseNoEncontrada();
    }
    return clase;
  }

  @Override
  public List<Reserva> obtenerInscriptos(Long idClase, Long idEntrenador)
    throws ClaseNoEncontrada, ClaseNoPermitida {
    Clase clase = obtenerPorId(idClase);

    // Validación: la clase debe pertenecer al entrenador logueado
    if (clase.getEntrenador() == null || !clase.getEntrenador().getId().equals(idEntrenador)) {
      throw new ClaseNoPermitida();
    }

    return repositorioReserva.buscarPorClase(idClase);
  }

  @Override
  public void cancelarClase(Long idClase, Long idEntrenador)
    throws ClaseNoEncontrada, ClaseNoPermitida {
    Clase clase = repositorioClase.buscarPorId(idClase);
    if (clase == null) {
      throw new ClaseNoEncontrada();
    }
    if (clase.getEntrenador() == null || !clase.getEntrenador().getId().equals(idEntrenador)) {
      throw new ClaseNoPermitida();
    }

    // Cancelar todas las reservas asociadas a esta clase
    List<Reserva> reservas = repositorioReserva.buscarPorClase(idClase);
    for (Reserva reserva : reservas) {
      if (reserva.getEstado() != EstadoReserva.CANCELADA) {
        reserva.setEstado(EstadoReserva.CANCELADA);
        repositorioReserva.modificar(reserva);
      }
    }

    // Se eliminan las reservas o se borra la clase según la regla que prefieras
    // Para este caso eliminamos la clase cancelada:
    repositorioClase.eliminar(clase);
  }
}
