package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.Reserva;
import java.util.List;

public interface RepositorioReserva {
  void guardar(Reserva reserva);
  void modificar(Reserva reserva);
  List<Reserva> buscarPorSocio(Long socioId);
  int contarConfirmadas(Long claseId);
  boolean existeConfirmada(Long socioId, Long claseId);
  Reserva buscarPorId(Long id);
}
