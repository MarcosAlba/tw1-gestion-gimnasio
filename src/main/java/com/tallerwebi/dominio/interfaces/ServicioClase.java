package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.Clase;
import com.tallerwebi.dominio.Reserva;
import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.excepcion.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ServicioClase {
  void crear(Clase clase, Long idEntrenador)
    throws EntrenadorInvalido, FechaClaseInvalida, ClaseSuperpuesta, LugarOcupado;
  List<Clase> listarProximas();
  Map<LocalDate, List<Clase>> listarSemana(CapacidadFisica capacidad, String filtroTexto);

  Clase obtenerPorId(Long idClase) throws ClaseNoEncontrada;
  List<Reserva> obtenerInscriptos(Long idClase, Long idEntrenador)
    throws ClaseNoEncontrada, ClaseNoPermitida;
  void cancelarClase(Long idClase, Long idEntrenador) throws ClaseNoEncontrada, ClaseNoPermitida;
}
