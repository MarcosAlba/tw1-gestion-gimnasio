package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.Clase;
import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.excepcion.EntrenadorInvalido;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ServicioClase {
  void crear(Clase clase, Long entrenadorId) throws EntrenadorInvalido;
  List<Clase> listarProximas();
  Map<LocalDate, List<Clase>> listarSemana(CapacidadFisica capacidad);
}
