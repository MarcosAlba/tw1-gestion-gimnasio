package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.Clase;
import java.time.LocalDateTime;
import java.util.List;

public interface RepositorioClase {
  void guardar(Clase clase);
  Clase buscarPorId(Long id);
  List<Clase> buscarDesde(LocalDateTime desde);
}
