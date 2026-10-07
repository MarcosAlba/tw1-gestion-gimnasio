package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.RolInvalido;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.interfaces.ServicioLogin;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ControladorLoginTest {

  private ControladorLogin controladorLogin;
  private Usuario usuarioMock;
  private DatosLogin datosLoginMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;
  private ServicioLogin servicioLoginMock;

  @BeforeEach
  public void init() {
    datosLoginMock = new DatosLogin("dami@unlam.com", "123");
    usuarioMock = mock(Usuario.class);
    when(usuarioMock.getEmail()).thenReturn("dami@unlam.com");
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    servicioLoginMock = mock(ServicioLogin.class);
    controladorLogin = new ControladorLogin(servicioLoginMock);
  }

  @Test
  public void loginConUsuarioYPasswordInorrectosDeberiaLlevarALoginNuevamente() {
    // preparacion
    when(servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(null);

    // ejecucion
    ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock, null);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Usuario o clave incorrecta")
    );
    verify(sessionMock, times(0)).setAttribute("ROL", "ADMIN");
  }

  @Test
  public void loginConUsuarioYPasswordCorrectosDeberiaLLevarAHome() {
    // preparacion
    Usuario usuarioEncontradoMock = mock(Usuario.class);
    when(usuarioEncontradoMock.getRol()).thenReturn("ADMIN");
    when(usuarioEncontradoMock.getId()).thenReturn(1L);

    when(requestMock.getSession()).thenReturn(sessionMock);
    when(servicioLoginMock.consultarUsuario(anyString(), anyString()))
      .thenReturn(usuarioEncontradoMock);

    ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock, null);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
    verify(sessionMock, times(1)).setAttribute("ROL", usuarioEncontradoMock.getRol());
    verify(sessionMock, times(1)).setAttribute("ID_USUARIO", 1L);
  }

  @Test
  public void registrameSiUsuarioNoExisteDeberiaCrearUsuarioYVolverAlLogin() throws Exception {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.registrarme(usuarioMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
    verify(servicioLoginMock, times(1)).registrar(usuarioMock);
  }

  @Test
  public void registrarmeSiUsuarioExisteDeberiaVolverAFormularioYMostrarError() throws Exception {
    // preparacion
    doThrow(UsuarioExistente.class).when(servicioLoginMock).registrar(usuarioMock);

    // ejecucion
    ModelAndView modelAndView = controladorLogin.registrarme(usuarioMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("El usuario ya existe")
    );
  }

  @Test
  public void registrarmeConRolInvalidoDeberiaVolverAFormularioYMostrarError() throws Exception {
    doThrow(RolInvalido.class).when(servicioLoginMock).registrar(usuarioMock);

    ModelAndView modelAndView = controladorLogin.registrarme(usuarioMock);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("El rol elegido no es válido")
    );
  }

  @Test
  public void registrarmeConErrorDeberiaConservarLosDatosYaEscritos() throws Exception {
    doThrow(UsuarioExistente.class).when(servicioLoginMock).registrar(usuarioMock);

    ModelAndView modelAndView = controladorLogin.registrarme(usuarioMock);

    assertThat(modelAndView.getModel().get("usuario"), sameInstance(usuarioMock));
  }

  @Test
  public void errorEnRegistrarmeDeberiaVolverAFormularioYMostrarError() throws Exception {
    // preparacion
    doThrow(RuntimeException.class).when(servicioLoginMock).registrar(usuarioMock);

    // ejecucion
    ModelAndView modelAndView = controladorLogin.registrarme(usuarioMock);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Error al registrar el nuevo usuario")
    );
  }

  @Test
  public void irALoginDeberiaRetornarVistaLoginConDatosLogin() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.irALogin();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
    assertThat(modelAndView.getModel().get("datosLogin"), instanceOf(DatosLogin.class));
  }

  @Test
  public void nuevoUsuarioDeberiaRetornarVistaNuevoUsuarioConUsuarioVacio() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.nuevoUsuario();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
    assertThat(modelAndView.getModel().get("usuario"), instanceOf(Usuario.class));
  }

  @Test
  public void nuevoUsuarioDeberiaProponerElRolSocioPorDefecto() {
    ModelAndView modelAndView = controladorLogin.nuevoUsuario();

    Usuario usuario = (Usuario) modelAndView.getModel().get("usuario");
    assertThat(usuario.getRol(), equalToIgnoringCase("SOCIO"));
  }

  @Test
  public void irAHomeDeberiaRetornarVistaHome() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.irAHome();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("home"));
  }

  @Test
  public void inicioSinSesionDeberiaMostrarLaBienvenida() {
    when(requestMock.getSession()).thenReturn(sessionMock);
    when(sessionMock.getAttribute("ROL")).thenReturn(null);

    ModelAndView modelAndView = controladorLogin.inicio(requestMock);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("bienvenida"));
  }

  @Test
  public void inicioConSesionIniciadaDeberiaRedirigirAHome() {
    when(requestMock.getSession()).thenReturn(sessionMock);
    when(sessionMock.getAttribute("ROL")).thenReturn("SOCIO");

    ModelAndView modelAndView = controladorLogin.inicio(requestMock);

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
  }

  @Test
  public void loginCorrectoConVolverValidoDeberiaIrAlDestinoPedido() {
    Usuario usuarioEncontradoMock = mock(Usuario.class);
    when(usuarioEncontradoMock.getRol()).thenReturn("SOCIO");
    when(usuarioEncontradoMock.getId()).thenReturn(1L);

    when(requestMock.getSession()).thenReturn(sessionMock);
    when(servicioLoginMock.consultarUsuario(anyString(), anyString()))
      .thenReturn(usuarioEncontradoMock);

    ModelAndView modelAndView = controladorLogin.validarLogin(
      datosLoginMock,
      requestMock,
      "/rutina"
    );

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/rutina"));
  }

  @Test
  public void loginCorrectoConVolverExternoDeberiaIrAHome() {
    Usuario usuarioEncontradoMock = mock(Usuario.class);
    when(usuarioEncontradoMock.getRol()).thenReturn("SOCIO");
    when(usuarioEncontradoMock.getId()).thenReturn(1L);

    when(requestMock.getSession()).thenReturn(sessionMock);
    when(servicioLoginMock.consultarUsuario(anyString(), anyString()))
      .thenReturn(usuarioEncontradoMock);

    ModelAndView modelAndView = controladorLogin.validarLogin(
      datosLoginMock,
      requestMock,
      "https://otro.com"
    );

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
  }

  @Test
  public void loginCorrectoConVolverDobleBarraDeberiaIrAHome() {
    Usuario usuarioEncontradoMock = mock(Usuario.class);
    when(usuarioEncontradoMock.getRol()).thenReturn("SOCIO");
    when(usuarioEncontradoMock.getId()).thenReturn(1L);

    when(requestMock.getSession()).thenReturn(sessionMock);
    when(servicioLoginMock.consultarUsuario(anyString(), anyString()))
      .thenReturn(usuarioEncontradoMock);

    ModelAndView modelAndView = controladorLogin.validarLogin(
      datosLoginMock,
      requestMock,
      "//otro.com"
    );

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
  }
}
