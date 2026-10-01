package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Rutina;
import com.tallerwebi.dominio.excepcion.SocioSinDeporte;
import com.tallerwebi.dominio.interfaces.ServicioRutina;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorRutinaTest {

  private ControladorRutina controlador;
  private ServicioRutina servicioMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;

  @BeforeEach
  public void init() {
    servicioMock = mock(ServicioRutina.class);
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    when(requestMock.getSession()).thenReturn(sessionMock);
    controlador = new ControladorRutina(servicioMock);
  }

  @Test
  public void deberiaMostrarLaUltimaRutinaSiEsSocio() {
    dadoQueSoy("SOCIO", 1L);
    Rutina rutina = new Rutina();
    when(servicioMock.obtenerUltima(1L)).thenReturn(rutina);

    ModelAndView modelAndView = controlador.verRutina(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("rutina"));
    assertThat(modelAndView.getModel().get("rutina"), equalTo(rutina));
  }

  @Test
  public void noDeberiaMostrarLaRutinaSiNoEsSocio() {
    dadoQueSoy("ENTRENADOR", 2L);

    ModelAndView modelAndView = controlador.verRutina(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
    verify(servicioMock, never()).obtenerUltima(any());
  }

  @Test
  public void deberiaGenerarUnaRutinaYRedirigirAVerla() throws Exception {
    dadoQueSoy("SOCIO", 1L);

    ModelAndView modelAndView = controlador.generarRutina(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/rutina"));
    verify(servicioMock, times(1)).generar(1L);
  }

  @Test
  public void deberiaMostrarUnErrorSiElSocioNoTieneDeporte() throws Exception {
    dadoQueSoy("SOCIO", 1L);
    doThrow(new SocioSinDeporte()).when(servicioMock).generar(1L);

    ModelAndView modelAndView = controlador.generarRutina(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("rutina"));
    assertThat(
      modelAndView.getModel().get("error"),
      equalTo("No tenés un deporte asociado para generar la rutina")
    );
  }

  @Test
  public void noDeberiaGenerarUnaRutinaSiNoEsSocio() throws Exception {
    dadoQueSoy("ENTRENADOR", 2L);

    ModelAndView modelAndView = controlador.generarRutina(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
    verify(servicioMock, never()).generar(any());
  }

  private void dadoQueSoy(String rol, Long id) {
    when(sessionMock.getAttribute("ROL")).thenReturn(rol);
    when(sessionMock.getAttribute("ID_USUARIO")).thenReturn(id);
  }
}
