package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Reserva;
import com.tallerwebi.dominio.excepcion.ClaseNoEncontrada;
import com.tallerwebi.dominio.excepcion.MembresiaNoVigente;
import com.tallerwebi.dominio.excepcion.ReservaDuplicada;
import com.tallerwebi.dominio.excepcion.ReservaNoEncontrada;
import com.tallerwebi.dominio.interfaces.ServicioReserva;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorReservaTest {

  private ControladorReserva controlador;
  private ServicioReserva servicioMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;

  @BeforeEach
  public void init() {
    servicioMock = mock(ServicioReserva.class);
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    when(requestMock.getSession()).thenReturn(sessionMock);
    controlador = new ControladorReserva(servicioMock);
  }

  @Test
  public void deberiaMostrarLasReservasDelSocio() {
    dadoQueSoy("SOCIO", 1L);
    List<Reserva> reservas = List.of(new Reserva());
    when(servicioMock.misReservas(1L)).thenReturn(reservas);

    ModelAndView modelAndView = controlador.verReservas(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("reservas"));
    assertThat(modelAndView.getModel().get("reservas"), equalTo(reservas));
    assertThat(modelAndView.getModel().get("error"), nullValue());
  }

  @Test
  public void noDeberiaMostrarLasReservasSiNoEsSocio() {
    dadoQueSoy("ENTRENADOR", 2L);

    ModelAndView modelAndView = controlador.verReservas(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
    verify(servicioMock, never()).misReservas(any());
  }

  @Test
  public void deberiaReservarYRedirigirAMisReservas() throws Exception {
    dadoQueSoy("SOCIO", 1L);

    ModelAndView modelAndView = controlador.reservar(5L, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/reservas"));
    verify(servicioMock, times(1)).reservar(1L, 5L);
  }

  @Test
  public void deberiaMostrarUnErrorSiNoTieneMembresiaVigente() throws Exception {
    dadoQueSoy("SOCIO", 1L);
    doThrow(new MembresiaNoVigente()).when(servicioMock).reservar(1L, 5L);

    ModelAndView modelAndView = controlador.reservar(5L, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("reservas"));
    assertThat(modelAndView.getModel().get("error"), equalTo("No tenés una membresía vigente"));
  }

  @Test
  public void deberiaMostrarUnErrorSiLaReservaEstaDuplicada() throws Exception {
    dadoQueSoy("SOCIO", 1L);
    doThrow(new ReservaDuplicada()).when(servicioMock).reservar(1L, 5L);

    ModelAndView modelAndView = controlador.reservar(5L, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("reservas"));
    assertThat(
      modelAndView.getModel().get("error"),
      equalTo("Ya tenés un lugar o estás en lista de espera para esta clase")
    );
  }

  @Test
  public void deberiaMostrarUnErrorSiLaClaseNoExiste() throws Exception {
    dadoQueSoy("SOCIO", 1L);
    doThrow(new ClaseNoEncontrada()).when(servicioMock).reservar(1L, 5L);

    ModelAndView modelAndView = controlador.reservar(5L, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("reservas"));
    assertThat(modelAndView.getModel().get("error"), equalTo("La clase no existe"));
  }

  @Test
  public void noDeberiaReservarSiNoEsSocio() throws Exception {
    dadoQueSoy("ENTRENADOR", 2L);

    ModelAndView modelAndView = controlador.reservar(5L, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
    verify(servicioMock, never()).reservar(any(), any());
  }

  @Test
  public void deberiaCancelarYRedirigirAMisReservas() {
    dadoQueSoy("SOCIO", 1L);

    ModelAndView modelAndView = controlador.cancelar(7L, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/reservas"));
    verify(servicioMock, times(1)).cancelar(1L, 7L);
  }

  @Test
  public void deberiaMostrarUnErrorSiLaReservaACancelarNoExiste() {
    dadoQueSoy("SOCIO", 1L);
    doThrow(new ReservaNoEncontrada()).when(servicioMock).cancelar(1L, 7L);

    ModelAndView modelAndView = controlador.cancelar(7L, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("reservas"));
    assertThat(modelAndView.getModel().get("error"), equalTo("No se encontró la reserva"));
  }

  @Test
  public void noDeberiaCancelarSiNoEsSocio() {
    dadoQueSoy("ENTRENADOR", 2L);

    ModelAndView modelAndView = controlador.cancelar(7L, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
    verify(servicioMock, never()).cancelar(any(), any());
  }

  private void dadoQueSoy(String rol, Long id) {
    when(sessionMock.getAttribute("ROL")).thenReturn(rol);
    when(sessionMock.getAttribute("ID_USUARIO")).thenReturn(id);
  }
}
