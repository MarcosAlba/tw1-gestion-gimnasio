package com.tallerwebi.dominio;

import com.tallerwebi.dominio.enums.EstadoReserva;
import com.tallerwebi.dominio.excepcion.ClaseNoEncontrada;
import com.tallerwebi.dominio.excepcion.ClaseSinCupo;
import com.tallerwebi.dominio.excepcion.MembresiaNoVigente;
import com.tallerwebi.dominio.excepcion.ReservaDuplicada;
import com.tallerwebi.dominio.excepcion.ReservaNoEncontrada;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.interfaces.RepositorioClase;
import com.tallerwebi.dominio.interfaces.RepositorioMembresia;
import com.tallerwebi.dominio.interfaces.RepositorioReserva;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioReserva;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioReserva")
@Transactional
public class ServicioReservaImpl implements ServicioReserva {

  private RepositorioReserva repoReserva;
  private RepositorioMembresia repoMembresia;
  private RepositorioClase repoClase;
  private RepositorioUsuario repoUsuario;

  @Autowired
  public ServicioReservaImpl(
    RepositorioReserva repoReserva,
    RepositorioMembresia repoMembresia,
    RepositorioClase repoClase,
    RepositorioUsuario repoUsuario
  ) {
    this.repoReserva = repoReserva;
    this.repoMembresia = repoMembresia;
    this.repoClase = repoClase;
    this.repoUsuario = repoUsuario;
  }

  @Override
  public void reservar(Long socioId, Long claseId)
    throws MembresiaNoVigente, ClaseSinCupo, ReservaDuplicada {
    Usuario socio = buscarSocio(socioId);
    Clase clase = buscarClase(claseId);
    validarQueSePuedeReservar(socioId, clase);

    Reserva reserva = new Reserva();
    reserva.setSocio(socio);
    reserva.setClase(clase);
    reserva.setEstado(EstadoReserva.CONFIRMADA);
    reserva.setFechaReserva(LocalDateTime.now());
    repoReserva.guardar(reserva);

    clase.setCupo(clase.getCupo() - 1);
    repoClase.modificar(clase);
  }

  private Usuario buscarSocio(Long socioId) {
    Usuario socio = repoUsuario.buscarPorId(socioId);
    if (socio == null) {
      throw new UsuarioNoEncontrado();
    }
    return socio;
  }

  private Clase buscarClase(Long claseId) {
    Clase clase = repoClase.buscarPorId(claseId);
    if (clase == null) {
      throw new ClaseNoEncontrada();
    }
    return clase;
  }

  private void validarQueSePuedeReservar(Long socioId, Clase clase)
    throws MembresiaNoVigente, ClaseSinCupo, ReservaDuplicada {
    if (repoMembresia.buscarVigente(socioId, LocalDate.now()) == null) {
      throw new MembresiaNoVigente();
    }
    if (repoReserva.existeConfirmada(socioId, clase.getId())) {
      throw new ReservaDuplicada();
    }
    if (repoReserva.contarConfirmadas(clase.getId()) >= clase.getCupo()) {
      throw new ClaseSinCupo();
    }
  }

  @Override
  public void cancelar(Long socioId, Long reservaId) {
    Reserva reserva = repoReserva.buscarPorId(reservaId);
    if (reserva == null || !reserva.getSocio().getId().equals(socioId)) {
      throw new ReservaNoEncontrada();
    }
    reserva.setEstado(EstadoReserva.CANCELADA);
    repoReserva.modificar(reserva);

    Clase clase = reserva.getClase();
    clase.setCupo(clase.getCupo() + 1);
    repoClase.modificar(clase);
  }

  @Override
  public List<Reserva> misReservas(Long socioId) {
    return repoReserva.buscarPorSocio(socioId);
  }
}
