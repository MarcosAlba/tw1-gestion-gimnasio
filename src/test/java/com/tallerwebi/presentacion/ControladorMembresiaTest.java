package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Membresia;
import com.tallerwebi.dominio.enums.TipoMembresia;
import com.tallerwebi.dominio.interfaces.ServicioMembresia;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorMembresiaTest {

  private ControladorMembresia controlador;
  private ServicioMembresia servicioMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;

  @BeforeEach
  public void init() {
    servicioMock = mock(ServicioMembresia.class);
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    when(requestMock.getSession()).thenReturn(sessionMock);
    controlador = new ControladorMembresia(servicioMock);
  }

  @Test
  public void deberiaMostrarElHistorialYLaMembresiaVigenteSiEsSocio() {
    dadoQueSoy("SOCIO", 1L);
    List<Membresia> historial = List.of(new Membresia());
    Membresia vigente = new Membresia();
    when(servicioMock.historial(1L)).thenReturn(historial);
    when(servicioMock.obtenerVigente(1L)).thenReturn(vigente);

    ModelAndView modelAndView = controlador.verMembresias(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("membresias"));
    assertThat(modelAndView.getModel().get("historial"), equalTo(historial));
    assertThat(modelAndView.getModel().get("vigente"), equalTo(vigente));
  }

  @Test
  public void noDeberiaMostrarLasMembresiasSiNoEsSocio() {
    dadoQueSoy("ENTRENADOR", 2L);

    ModelAndView modelAndView = controlador.verMembresias(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
    verify(servicioMock, never()).historial(any());
  }

  @Test
  public void deberiaContratarUnaMembresiaYRedirigirAlListado() {
    dadoQueSoy("SOCIO", 1L);

    ModelAndView modelAndView = controlador.contratarMembresia(TipoMembresia.MENSUAL, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/membresias"));
    verify(servicioMock, times(1)).registrar(1L, TipoMembresia.MENSUAL);
  }

  @Test
  public void noDeberiaContratarUnaMembresiaSiNoEsSocio() {
    dadoQueSoy("ENTRENADOR", 2L);

    ModelAndView modelAndView = controlador.contratarMembresia(TipoMembresia.MENSUAL, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
    verify(servicioMock, never()).registrar(any(), any());
  }

  @Test
  public void deberiaMostrarHistorialVacioYSinMembresiaVigenteSiElSocioEsNuevo() {
    dadoQueSoy("SOCIO", 10L);
    List<Membresia> historialVacio = new ArrayList<>();
    when(servicioMock.historial(10L)).thenReturn(historialVacio);
    when(servicioMock.obtenerVigente(10L)).thenReturn(null);

    ModelAndView modelAndView = controlador.verMembresias(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("membresias"));
    assertThat(modelAndView.getModel().get("historial"), equalTo(historialVacio));
    assertThat(modelAndView.getModel().get("vigente"), equalTo(null));
  }

  private void dadoQueSoy(String rol, Long id) {
    when(sessionMock.getAttribute("ROL")).thenReturn(rol);
    when(sessionMock.getAttribute("ID_USUARIO")).thenReturn(id);
  }
}
