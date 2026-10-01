package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.Rutina;
import com.tallerwebi.dominio.excepcion.SocioSinDeporte;

public interface ServicioRutina {
  Rutina generar(Long socioId) throws SocioSinDeporte;
  Rutina obtenerUltima(Long socioId);
}
