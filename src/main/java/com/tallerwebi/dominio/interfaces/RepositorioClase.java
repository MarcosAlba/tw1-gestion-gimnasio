package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.Clase;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface RepositorioClase {
  void guardar(Clase clase);
  void modificar(Clase clase);
  Clase buscarPorId(Long id);
  List<Clase> buscarDesde(LocalDateTime desde);
  List<Clase> buscarPorEntrenadorYFecha(Long idEntrenador, LocalDate fecha);
  List<Clase> buscarPorLugarYFecha(String lugar, LocalDate fecha);
  void eliminar(Clase clase);
}
