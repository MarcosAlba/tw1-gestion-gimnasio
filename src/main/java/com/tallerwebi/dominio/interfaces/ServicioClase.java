package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.Clase;
import com.tallerwebi.dominio.excepcion.EntrenadorInvalido;
import java.util.List;

public interface ServicioClase {
  void crear(Clase clase, Long entrenadorId) throws EntrenadorInvalido;
  List<Clase> listarProximas();
}
