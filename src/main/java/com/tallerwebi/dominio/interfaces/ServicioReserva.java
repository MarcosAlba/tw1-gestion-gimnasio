package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.Reserva;
import com.tallerwebi.dominio.excepcion.MembresiaNoVigente;
import com.tallerwebi.dominio.excepcion.ReservaDuplicada;
import java.util.List;

public interface ServicioReserva {
  void reservar(Long socioId, Long claseId) throws MembresiaNoVigente, ReservaDuplicada;
  void cancelar(Long socioId, Long reservaId);
  List<Reserva> misReservas(Long socioId);
}
