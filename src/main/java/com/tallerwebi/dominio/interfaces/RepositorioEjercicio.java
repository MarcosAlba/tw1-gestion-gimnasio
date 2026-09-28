package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.Ejercicio;
import com.tallerwebi.dominio.enums.CapacidadFisica;
import java.util.List;

public interface RepositorioEjercicio {
  void guardar(Ejercicio ejercicio);
  Ejercicio buscarPorNombre(String nombre);
  List<Ejercicio> buscarTodos();
  List<Ejercicio> buscarPorCapacidad(CapacidadFisica capacidad);
}
