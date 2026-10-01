package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Clase;
import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.excepcion.EntrenadorInvalido;
import com.tallerwebi.dominio.interfaces.ServicioClase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorClaseTest {

  private ControladorClase controlador;
  private ServicioClase servicioMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;

  @BeforeEach
  public void init() {
    servicioMock = mock(ServicioClase.class);
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    when(requestMock.getSession()).thenReturn(sessionMock);
    controlador = new ControladorClase(servicioMock);
  }

  @Test
  public void deberiaMostrarElFormularioDeAltaSiEsEntrenador() {
    dadoQueSoy("ENTRENADOR", 1L);

    ModelAndView modelAndView = controlador.irANuevaClase(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("nueva-clase"));
    assertThat(modelAndView.getModel().get("datosClase"), notNullValue());
  }

  @Test
  public void noDeberiaMostrarElFormularioDeAltaSiNoEsEntrenador() {
    dadoQueSoy("SOCIO", 2L);

    ModelAndView modelAndView = controlador.irANuevaClase(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
  }

  @Test
  public void deberiaGuardarLaClaseYRedirigirAlListado() throws Exception {
    dadoQueSoy("ENTRENADOR", 1L);
    DatosClase datos = dadoQueTengoLosDatosDeUnaClase();

    ModelAndView modelAndView = controlador.guardarClase(datos, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/clases"));
    verify(servicioMock, times(1)).crear(any(Clase.class), eq(1L));
  }

  @Test
  public void deberiaPasarLosDatosDelFormularioALaClase() throws Exception {
    dadoQueSoy("ENTRENADOR", 1L);
    DatosClase datos = dadoQueTengoLosDatosDeUnaClase();
    org.mockito.ArgumentCaptor<Clase> captor = org.mockito.ArgumentCaptor.forClass(Clase.class);

    controlador.guardarClase(datos, requestMock);

    verify(servicioMock).crear(captor.capture(), eq(1L));
    Clase clase = captor.getValue();
    assertThat(clase.getNombre(), equalTo("Spinning"));
    assertThat(clase.getInicio(), equalTo(datos.getInicio()));
    assertThat(clase.getDuracion(), equalTo(45));
    assertThat(clase.getLugar(), equalTo("Sala 2"));
    assertThat(clase.getCupo(), equalTo(15));
    assertThat(clase.getCapacidad(), equalTo(CapacidadFisica.CARDIO));
  }

  @Test
  public void deberiaVolverAlFormularioSiElEntrenadorEsInvalido() throws Exception {
    dadoQueSoy("ENTRENADOR", 1L);
    DatosClase datos = dadoQueTengoLosDatosDeUnaClase();
    doThrow(new EntrenadorInvalido()).when(servicioMock).crear(any(Clase.class), eq(1L));

    ModelAndView modelAndView = controlador.guardarClase(datos, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("nueva-clase"));
    assertThat(
      modelAndView.getModel().get("error"),
      equalTo("El usuario no es un entrenador valido")
    );
    assertThat(modelAndView.getModel().get("datosClase"), equalTo(datos));
  }

  @Test
  public void noDeberiaGuardarLaClaseSiNoEsEntrenador() throws Exception {
    dadoQueSoy("SOCIO", 2L);
    DatosClase datos = dadoQueTengoLosDatosDeUnaClase();

    ModelAndView modelAndView = controlador.guardarClase(datos, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
    verify(servicioMock, never()).crear(any(Clase.class), any());
  }

  @Test
  public void deberiaListarLasProximasClasesSiHayUnUsuarioLogueado() {
    dadoQueSoy("SOCIO", 2L);
    List<Clase> clases = List.of(new Clase(), new Clase());
    when(servicioMock.listarProximas()).thenReturn(clases);

    ModelAndView modelAndView = controlador.listarClases(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("clases"));
    assertThat(modelAndView.getModel().get("clases"), equalTo(clases));
  }

  @Test
  public void noDeberiaListarLasClasesSiNoHayUsuarioLogueado() {
    when(sessionMock.getAttribute("ROL")).thenReturn(null);

    ModelAndView modelAndView = controlador.listarClases(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
    verify(servicioMock, never()).listarProximas();
  }

  private void dadoQueSoy(String rol, Long id) {
    when(sessionMock.getAttribute("ROL")).thenReturn(rol);
    when(sessionMock.getAttribute("ID_USUARIO")).thenReturn(id);
  }

  private DatosClase dadoQueTengoLosDatosDeUnaClase() {
    DatosClase datos = new DatosClase();
    datos.setNombre("Spinning");
    datos.setInicio(LocalDateTime.now().plusDays(1));
    datos.setDuracion(45);
    datos.setLugar("Sala 2");
    datos.setCupo(15);
    datos.setCapacidad(CapacidadFisica.CARDIO);
    return datos;
  }
}
