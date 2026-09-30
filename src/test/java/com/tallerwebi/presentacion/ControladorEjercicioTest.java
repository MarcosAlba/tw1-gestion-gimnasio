package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Ejercicio;
import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.enums.NivelDificultad;
import com.tallerwebi.dominio.excepcion.EjercicioExistente;
import com.tallerwebi.dominio.excepcion.NombreEjercicioInvalido;
import com.tallerwebi.dominio.interfaces.ServicioEjercicio;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorEjercicioTest {

  private ControladorEjercicio controlador;
  private ServicioEjercicio servicioMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;

  @BeforeEach
  public void init() {
    servicioMock = mock(ServicioEjercicio.class);
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    when(requestMock.getSession()).thenReturn(sessionMock);
    controlador = new ControladorEjercicio(servicioMock);
  }

  @Test
  public void deberiaMostrarElFormularioDeAltaSiEsEntrenador() {
    dadoQueElRolEs("ENTRENADOR");

    ModelAndView modelAndView = controlador.irANuevoEjercicio(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("nuevo-ejercicio"));
    assertThat(modelAndView.getModel().get("datosEjercicio"), notNullValue());
  }

  @Test
  public void noDeberiaMostrarElFormularioDeAltaSiNoEsEntrenador() {
    dadoQueElRolEs("SOCIO");

    ModelAndView modelAndView = controlador.irANuevoEjercicio(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
  }

  @Test
  public void deberiaGuardarUnEjercicioYRedirigirAlListado() throws Exception {
    dadoQueElRolEs("ENTRENADOR");
    DatosEjercicio datos = dadoQueTengoLosDatosDeUnEjercicio("Sentadilla");

    ModelAndView modelAndView = controlador.guardarEjercicio(datos, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/ejercicios"));
    verify(servicioMock, times(1)).registrar(any(Ejercicio.class));
  }

  @Test
  public void deberiaVolverAlFormularioSiElNombreEsInvalido() throws Exception {
    dadoQueElRolEs("ENTRENADOR");
    DatosEjercicio datos = dadoQueTengoLosDatosDeUnEjercicio("");
    doThrow(new NombreEjercicioInvalido()).when(servicioMock).registrar(any(Ejercicio.class));

    ModelAndView modelAndView = controlador.guardarEjercicio(datos, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("nuevo-ejercicio"));
    assertThat(
      modelAndView.getModel().get("error"),
      equalTo("El nombre del ejercicio es obligatorio")
    );
    assertThat(modelAndView.getModel().get("datosEjercicio"), equalTo(datos));
  }

  @Test
  public void deberiaVolverAlFormularioSiElEjercicioYaExiste() throws Exception {
    dadoQueElRolEs("ENTRENADOR");
    DatosEjercicio datos = dadoQueTengoLosDatosDeUnEjercicio("Burpees");
    doThrow(new EjercicioExistente()).when(servicioMock).registrar(any(Ejercicio.class));

    ModelAndView modelAndView = controlador.guardarEjercicio(datos, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("nuevo-ejercicio"));
    assertThat(
      modelAndView.getModel().get("error"),
      equalTo("Ya existe un ejercicio con ese nombre")
    );
    assertThat(modelAndView.getModel().get("datosEjercicio"), equalTo(datos));
  }

  @Test
  public void noDeberiaGuardarUnEjercicioSiNoEsEntrenador() throws Exception {
    dadoQueElRolEs("SOCIO");
    DatosEjercicio datos = dadoQueTengoLosDatosDeUnEjercicio("Sentadilla");

    ModelAndView modelAndView = controlador.guardarEjercicio(datos, requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
    verify(servicioMock, never()).registrar(any(Ejercicio.class));
  }

  @Test
  public void deberiaListarLosEjerciciosSiEsEntrenador() {
    dadoQueElRolEs("ENTRENADOR");
    List<Ejercicio> ejercicios = List.of(new Ejercicio(), new Ejercicio());
    when(servicioMock.listarTodos()).thenReturn(ejercicios);

    ModelAndView modelAndView = controlador.listarEjercicios(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("ejercicios"));
    assertThat(modelAndView.getModel().get("ejercicios"), equalTo(ejercicios));
  }

  @Test
  public void noDeberiaListarLosEjerciciosSiNoEsEntrenador() {
    dadoQueElRolEs("SOCIO");

    ModelAndView modelAndView = controlador.listarEjercicios(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
    verify(servicioMock, never()).listarTodos();
  }

  private void dadoQueElRolEs(String rol) {
    when(sessionMock.getAttribute("ROL")).thenReturn(rol);
  }

  private DatosEjercicio dadoQueTengoLosDatosDeUnEjercicio(String nombre) {
    DatosEjercicio datos = new DatosEjercicio();
    datos.setNombre(nombre);
    datos.setDescripcion("Descripcion de " + nombre);
    datos.setCapacidad(CapacidadFisica.FUERZA);
    datos.setDificultad(NivelDificultad.INTERMEDIO);
    return datos;
  }
}
