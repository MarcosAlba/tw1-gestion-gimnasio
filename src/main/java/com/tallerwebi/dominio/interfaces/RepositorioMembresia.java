package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.Membresia;
import java.time.LocalDate;
import java.util.List;

public interface RepositorioMembresia {
  void guardar(Membresia membresia);
  List<Membresia> buscarPorSocio(Long socioId);
  Membresia buscarVigente(Long socioId, LocalDate fecha);
}
