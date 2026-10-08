package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.enums.EstadoReserva;
import com.tallerwebi.dominio.excepcion.ClaseNoEncontrada;
import com.tallerwebi.dominio.excepcion.MembresiaNoVigente;
import com.tallerwebi.dominio.excepcion.ReservaDuplicada;
import com.tallerwebi.dominio.excepcion.ReservaNoEncontrada;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.interfaces.RepositorioClase;
import com.tallerwebi.dominio.interfaces.RepositorioMembresia;
import com.tallerwebi.dominio.interfaces.RepositorioReserva;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioReserva;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.annotation.Rollback;

public class ServicioReservaTest {

  private RepositorioReserva repoReservaMock;
  private RepositorioMembresia repoMembresiaMock;
  private RepositorioClase repoClaseMock;
  private RepositorioUsuario repoUsuarioMock;
  private ServicioReserva servicio;
  private Usuario socio;
  private Clase clase;

  @BeforeEach
  public void init() {
    repoReservaMock = mock(RepositorioReserva.class);
    repoMembresiaMock = mock(RepositorioMembresia.class);
    repoClaseMock = mock(RepositorioClase.class);
    repoUsuarioMock = mock(RepositorioUsuario.class);
    servicio =
      new ServicioReservaImpl(repoReservaMock, repoMembresiaMock, repoClaseMock, repoUsuarioMock);

    socio = new Usuario();
    socio.setId(1L);
    clase = new Clase();
    clase.setId(5L);
    clase.setCupo(2);
    when(repoUsuarioMock.buscarPorId(1L)).thenReturn(socio);
    when(repoClaseMock.buscarPorId(5L)).thenReturn(clase);
    when(repoMembresiaMock.buscarVigente(eq(1L), any(LocalDate.class))).thenReturn(new Membresia());
  }

  @Test
  public void deberiaReservarUnaClaseConfirmandolaYGuardandola() throws Exception {
    when(repoReservaMock.existeActiva(1L, 5L)).thenReturn(false);
    when(repoReservaMock.contarConfirmadas(5L)).thenReturn(1);

    servicio.reservar(1L, 5L);

    ArgumentCaptor<Reserva> captor = ArgumentCaptor.forClass(Reserva.class);
    verify(repoReservaMock, times(1)).guardar(captor.capture());
    Reserva guardada = captor.getValue();
    assertThat(guardada.getSocio(), equalTo(socio));
    assertThat(guardada.getClase(), equalTo(clase));
    assertThat(guardada.getEstado(), equalTo(EstadoReserva.CONFIRMADA));
    assertThat(guardada.getFechaReserva(), notNullValue());
  }

  @Test
  public void noDeberiaReservarSiElSocioNoTieneMembresiaVigente() {
    when(repoMembresiaMock.buscarVigente(eq(1L), any(LocalDate.class))).thenReturn(null);

    assertThrows(MembresiaNoVigente.class, () -> servicio.reservar(1L, 5L));
    verify(repoReservaMock, never()).guardar(any(Reserva.class));
  }

  @Test
  public void noDeberiaReservarSiYaTieneUnaReservaConfirmadaParaLaClase() {
    when(repoReservaMock.existeActiva(1L, 5L)).thenReturn(true);

    assertThrows(ReservaDuplicada.class, () -> servicio.reservar(1L, 5L));
    verify(repoReservaMock, never()).guardar(any(Reserva.class));
  }

  @Test
  public void deberiaAnotarAlSocioEnListaDeEsperaSiLaClaseEstaCompleta() throws Exception {
    clase.setCupo(0);
    when(repoReservaMock.existeActiva(1L, 5L)).thenReturn(false);

    servicio.reservar(1L, 5L);

    ArgumentCaptor<Reserva> captor = ArgumentCaptor.forClass(Reserva.class);
    verify(repoReservaMock, times(1)).guardar(captor.capture());
    assertThat(captor.getValue().getEstado(), equalTo(EstadoReserva.EN_ESPERA));
    assertThat(clase.getCupo(), equalTo(0));
    verify(repoClaseMock, never()).modificar(clase);
  }

  @Test
  public void noDeberiaAnotarDosVecesEnListaDeEsperaAlMismoSocio() {
    clase.setCupo(0);
    when(repoReservaMock.existeActiva(1L, 5L)).thenReturn(true);

    assertThrows(ReservaDuplicada.class, () -> servicio.reservar(1L, 5L));
    verify(repoReservaMock, never()).guardar(any(Reserva.class));
  }

  @Test
  public void noDeberiaAnotarEnListaDeEsperaSiElSocioNoTieneMembresiaVigente() {
    clase.setCupo(0);
    when(repoMembresiaMock.buscarVigente(eq(1L), any(LocalDate.class))).thenReturn(null);

    assertThrows(MembresiaNoVigente.class, () -> servicio.reservar(1L, 5L));
    verify(repoReservaMock, never()).guardar(any(Reserva.class));
  }

  @Test
  public void deberiaPermitirAlSocioSalirDeLaListaDeEspera() {
    Reserva reserva = dadoQueExisteUnaReservaDe(socio, 7L);
    reserva.setEstado(EstadoReserva.EN_ESPERA);

    servicio.cancelar(1L, 7L);

    assertThat(reserva.getEstado(), equalTo(EstadoReserva.CANCELADA));
    verify(repoReservaMock, times(1)).modificar(reserva);
  }

  @Test
  public void noDeberiaLiberarCupoSiElSocioSaleDeLaListaDeEspera() {
    clase.setCupo(0);
    Reserva reserva = new Reserva();
    reserva.setId(7L);
    reserva.setSocio(socio);
    reserva.setClase(clase);
    reserva.setEstado(EstadoReserva.EN_ESPERA);
    when(repoReservaMock.buscarPorId(7L)).thenReturn(reserva);

    servicio.cancelar(1L, 7L);

    assertThat(reserva.getEstado(), equalTo(EstadoReserva.CANCELADA));
    assertThat(clase.getCupo(), equalTo(0));
    verify(repoClaseMock, never()).modificar(clase);
  }

  @Test
  public void noDeberiaReservarSiLaClaseNoExiste() {
    when(repoClaseMock.buscarPorId(99L)).thenReturn(null);

    assertThrows(ClaseNoEncontrada.class, () -> servicio.reservar(1L, 99L));
    verify(repoReservaMock, never()).guardar(any(Reserva.class));
  }

  @Test
  public void noDeberiaReservarSiElSocioNoExiste() {
    when(repoUsuarioMock.buscarPorId(99L)).thenReturn(null);

    assertThrows(UsuarioNoEncontrado.class, () -> servicio.reservar(99L, 5L));
    verify(repoReservaMock, never()).guardar(any(Reserva.class));
  }

  @Test
  public void deberiaCancelarUnaReservaDelSocio() {
    Reserva reserva = dadoQueExisteUnaReservaDe(socio, 7L);

    servicio.cancelar(1L, 7L);

    assertThat(reserva.getEstado(), equalTo(EstadoReserva.CANCELADA));
    verify(repoReservaMock, times(1)).modificar(reserva);
  }

  @Test
  public void noDeberiaCancelarLaReservaDeOtroSocio() {
    Usuario otroSocio = new Usuario();
    otroSocio.setId(2L);
    dadoQueExisteUnaReservaDe(otroSocio, 7L);

    assertThrows(ReservaNoEncontrada.class, () -> servicio.cancelar(1L, 7L));
    verify(repoReservaMock, never()).modificar(any(Reserva.class));
  }

  @Test
  public void noDeberiaCancelarUnaReservaInexistente() {
    when(repoReservaMock.buscarPorId(99L)).thenReturn(null);

    assertThrows(ReservaNoEncontrada.class, () -> servicio.cancelar(1L, 99L));
    verify(repoReservaMock, never()).modificar(any(Reserva.class));
  }

  @Test
  public void deberiaObtenerLasReservasDelSocio() {
    List<Reserva> reservas = List.of(new Reserva(), new Reserva());
    when(repoReservaMock.buscarPorSocio(1L)).thenReturn(reservas);

    List<Reserva> resultado = servicio.misReservas(1L);

    assertThat(resultado, equalTo(reservas));
  }

  @Test
  public void deberiaRestarUnoAlCupoDeLaClaseAlReservar() throws Exception {
    clase.setCupo(5);
    when(repoReservaMock.existeActiva(1L, 5L)).thenReturn(false);
    when(repoReservaMock.contarConfirmadas(5L)).thenReturn(0);

    servicio.reservar(1L, 5L);

    assertThat(clase.getCupo(), equalTo(4));
    verify(repoClaseMock, times(1)).modificar(clase);
  }

  @Test
  public void deberiaSumarUnoAlCupoSiSeCancelaYNoHayNadieEnEspera() throws Exception {
    clase.setCupo(4);

    Reserva reserva = new Reserva();
    reserva.setId(7L);
    reserva.setSocio(socio);
    reserva.setClase(clase);
    reserva.setEstado(EstadoReserva.CONFIRMADA);

    when(repoReservaMock.buscarPorId(7L)).thenReturn(reserva);
    servicio.cancelar(1L, 7L);

    assertThat(clase.getCupo(), equalTo(5));
    verify(repoClaseMock, times(1)).modificar(clase);
  }

  private Reserva dadoQueExisteUnaReservaDe(Usuario duenio, Long reservaId) {
    Reserva reserva = new Reserva();
    reserva.setId(reservaId);
    reserva.setSocio(duenio);
    reserva.setClase(clase);
    reserva.setEstado(EstadoReserva.CONFIRMADA);
    when(repoReservaMock.buscarPorId(reservaId)).thenReturn(reserva);
    return reserva;
  }

  @Test
  public void deberiaAsignarElLugarLiberadoAlPrimeroDeLaListaDeEspera() {
    clase.setCupo(0);
    Reserva reservaQueSeCancela = dadoQueExisteUnaReservaDe(socio, 7L);
    Reserva primeraEnEspera = dadoQueHayUnaReservaEnEsperaDeOtroSocio();
    when(repoReservaMock.buscarPrimeraEnEspera(5L)).thenReturn(primeraEnEspera);

    servicio.cancelar(1L, 7L);

    assertThat(reservaQueSeCancela.getEstado(), equalTo(EstadoReserva.CANCELADA));
    assertThat(primeraEnEspera.getEstado(), equalTo(EstadoReserva.CONFIRMADA));
    verify(repoReservaMock, times(1)).modificar(primeraEnEspera);
    assertThat(clase.getCupo(), equalTo(0));
    verify(repoClaseMock, never()).modificar(clase);
  }

  @Test
  public void noDeberiaAsignarElLugarANadieSiQuienCancelaEstabaEnListaDeEspera() {
    Reserva reserva = dadoQueExisteUnaReservaDe(socio, 7L);
    reserva.setEstado(EstadoReserva.EN_ESPERA);

    servicio.cancelar(1L, 7L);

    verify(repoReservaMock, never()).buscarPrimeraEnEspera(anyLong());
  }

  private Reserva dadoQueHayUnaReservaEnEsperaDeOtroSocio() {
    Usuario otroSocio = new Usuario();
    otroSocio.setId(2L);
    Reserva reserva = new Reserva();
    reserva.setId(8L);
    reserva.setSocio(otroSocio);
    reserva.setClase(clase);
    reserva.setEstado(EstadoReserva.EN_ESPERA);
    return reserva;
  }
}
