package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.Membresia;
import com.tallerwebi.dominio.enums.TipoMembresia;
import java.util.List;

public interface ServicioMembresia {
  void registrar(Long socioId, TipoMembresia tipo);
  List<Membresia> historial(Long socioId);
  Membresia obtenerVigente(Long socioId);
  Long obtenerDiasRestantes(Long socioId);
}
