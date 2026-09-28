package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.Rutina;

public interface RepositorioRutina {
  void guardar(Rutina rutina);
  Rutina buscarUltimaDeSocio(Long socioId);
}
