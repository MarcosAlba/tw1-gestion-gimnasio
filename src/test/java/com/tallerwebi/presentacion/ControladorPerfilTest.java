package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.enums.Deporte;
import com.tallerwebi.dominio.excepcion.EdadInvalida;
import com.tallerwebi.dominio.excepcion.FotoDemasiadoGrande;
import com.tallerwebi.dominio.excepcion.FotoInvalida;
import com.tallerwebi.dominio.interfaces.ServicioPerfil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public class ControladorPerfilTest {

  private ControladorPerfil controlador;
  private ServicioPerfil servicioMock;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;
  private RedirectAttributes redirectMock;

  @BeforeEach
  public void init() {
    servicioMock = mock(ServicioPerfil.class);
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    redirectMock = mock(RedirectAttributes.class);
    when(requestMock.getSession()).thenReturn(sessionMock);
    controlador = new ControladorPerfil(servicioMock);
  }

  // ---------- GET /perfil (lectura) ----------

  @Test
  public void verPerfilSinSesionDeberiaIrAlLogin() {
    ModelAndView modelAndView = controlador.verPerfil(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
    verify(servicioMock, never()).obtener(any());
  }

  @Test
  public void verPerfilConSesionDeberiaMostrarLaVistaConElUsuario() {
    dadoQueSoy("SOCIO", 1L);
    Usuario usuario = unUsuario();
    when(servicioMock.obtener(1L)).thenReturn(usuario);

    ModelAndView modelAndView = controlador.verPerfil(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("perfil"));
    assertThat(modelAndView.getModel().get("usuario"), equalTo(usuario));
  }

  // ---------- GET /perfil/editar ----------

  @Test
  public void editarPerfilSinSesionDeberiaIrAlLogin() {
    ModelAndView modelAndView = controlador.editarPerfil(requestMock);

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
    verify(servicioMock, never()).obtener(any());
  }

  @Test
  public void editarPerfilDeberiaMostrarElFormularioConLosDatosActuales() {
    dadoQueSoy("SOCIO", 1L);
    Usuario usuario = unUsuario();
    when(servicioMock.obtener(1L)).thenReturn(usuario);

    ModelAndView modelAndView = controlador.editarPerfil(requestMock);

    DatosPerfil datos = (DatosPerfil) modelAndView.getModel().get("datosPerfil");
    assertThat(modelAndView.getViewName(), equalTo("perfil-editar"));
    assertThat(modelAndView.getModel().get("usuario"), equalTo(usuario));
    assertThat(datos.getNombre(), equalTo("Juan"));
    assertThat(datos.getApellido(), equalTo("Perez"));
    assertThat(datos.getEdad(), equalTo(30));
    assertThat(datos.getDeporte(), equalTo(Deporte.TENIS));
    assertThat(modelAndView.getModel().get("error"), nullValue());
  }

  // ---------- POST /perfil/guardar ----------

  @Test
  public void guardarSinSesionDeberiaIrAlLoginYNoGuardarNada() throws Exception {
    ModelAndView modelAndView = controlador.guardarPerfil(
      unosDatos(null),
      requestMock,
      redirectMock
    );

    assertThat(modelAndView.getViewName(), equalTo("redirect:/login"));
    verify(servicioMock, never()).actualizar(any(), any(), any(), any());
  }

  @Test
  public void guardarBienDeberiaVolverAlPerfilConUnMensaje() throws Exception {
    dadoQueSoy("SOCIO", 1L);

    ModelAndView modelAndView = controlador.guardarPerfil(
      unosDatos(null),
      requestMock,
      redirectMock
    );

    assertThat(modelAndView.getViewName(), equalTo("redirect:/perfil"));
    verify(redirectMock, times(1)).addFlashAttribute("exito", "Perfil actualizado.");
  }

  @Test
  public void guardarDeberiaUsarElIdDeLaSesionYLosDatosDelFormulario() throws Exception {
    dadoQueSoy("SOCIO", 7L);
    ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

    controlador.guardarPerfil(unosDatos(null), requestMock, redirectMock);

    verify(servicioMock).actualizar(eq(7L), captor.capture(), any(), any());
    Usuario cambios = captor.getValue();
    assertThat(cambios.getNombre(), equalTo("Juan"));
    assertThat(cambios.getApellido(), equalTo("Perez"));
    assertThat(cambios.getEdad(), equalTo(30));
    assertThat(cambios.getDeporte(), equalTo(Deporte.TENIS));
  }

  @Test
  public void guardarDeberiaPasarLosBytesYElTipoDeLaFoto() throws Exception {
    dadoQueSoy("SOCIO", 1L);
    byte[] bytes = { 1, 2, 3 };
    MultipartFile foto = new MockMultipartFile("foto", "perfil.jpg", "image/jpeg", bytes);

    controlador.guardarPerfil(unosDatos(foto), requestMock, redirectMock);

    verify(servicioMock).actualizar(eq(1L), any(Usuario.class), eq(bytes), eq("image/jpeg"));
  }

  @Test
  public void guardarSinFotoDeberiaPasarNulosAlServicio() throws Exception {
    dadoQueSoy("SOCIO", 1L);

    controlador.guardarPerfil(unosDatos(null), requestMock, redirectMock);

    verify(servicioMock).actualizar(eq(1L), any(Usuario.class), eq(null), eq(null));
  }

  @Test
  public void guardarConFotoMuyGrandeDeberiaVolverAlFormularioConElError() throws Exception {
    dadoQueSoy("SOCIO", 1L);
    when(servicioMock.obtener(1L)).thenReturn(unUsuario());
    doThrow(new FotoDemasiadoGrande()).when(servicioMock).actualizar(any(), any(), any(), any());
    DatosPerfil datos = unosDatos(null);

    ModelAndView modelAndView = controlador.guardarPerfil(datos, requestMock, redirectMock);

    assertThat(modelAndView.getViewName(), equalTo("perfil-editar"));
    assertThat(
      modelAndView.getModel().get("error"),
      equalTo("La foto pesa más de 2 MB. Elegí una más liviana.")
    );
    assertThat(modelAndView.getModel().get("datosPerfil"), equalTo(datos));
    verify(redirectMock, never()).addFlashAttribute(any(String.class), any());
  }

  @Test
  public void guardarConFotoInvalidaDeberiaVolverAlFormularioConElError() throws Exception {
    dadoQueSoy("SOCIO", 1L);
    when(servicioMock.obtener(1L)).thenReturn(unUsuario());
    doThrow(new FotoInvalida()).when(servicioMock).actualizar(any(), any(), any(), any());

    ModelAndView modelAndView = controlador.guardarPerfil(
      unosDatos(null),
      requestMock,
      redirectMock
    );

    assertThat(modelAndView.getViewName(), equalTo("perfil-editar"));
    assertThat(
      modelAndView.getModel().get("error"),
      equalTo("La foto tiene que ser JPG, PNG o WebP.")
    );
  }

  @Test
  public void guardarConEdadInvalidaDeberiaVolverAlFormularioConElError() throws Exception {
    dadoQueSoy("SOCIO", 1L);
    when(servicioMock.obtener(1L)).thenReturn(unUsuario());
    doThrow(new EdadInvalida()).when(servicioMock).actualizar(any(), any(), any(), any());

    ModelAndView modelAndView = controlador.guardarPerfil(
      unosDatos(null),
      requestMock,
      redirectMock
    );

    assertThat(modelAndView.getViewName(), equalTo("perfil-editar"));
    assertThat(
      modelAndView.getModel().get("error"),
      equalTo("La edad tiene que estar entre 1 y 120.")
    );
  }

  @Test
  public void guardarConUnaFotoIlegibleDeberiaVolverAlFormularioConElError() throws Exception {
    dadoQueSoy("SOCIO", 1L);
    when(servicioMock.obtener(1L)).thenReturn(unUsuario());
    MultipartFile fotoRota = mock(MultipartFile.class);
    when(fotoRota.getBytes()).thenThrow(new IOException());

    ModelAndView modelAndView = controlador.guardarPerfil(
      unosDatos(fotoRota),
      requestMock,
      redirectMock
    );

    assertThat(modelAndView.getViewName(), equalTo("perfil-editar"));
    assertThat(
      modelAndView.getModel().get("error"),
      equalTo("No pudimos leer la foto. Probá con otra.")
    );
    verify(servicioMock, never()).actualizar(any(), any(), any(), any());
  }

  // ---------- ayudas ----------

  private void dadoQueSoy(String rol, Long id) {
    when(sessionMock.getAttribute("ROL")).thenReturn(rol);
    when(sessionMock.getAttribute("ID_USUARIO")).thenReturn(id);
  }

  private Usuario unUsuario() {
    Usuario usuario = new Usuario();
    usuario.setId(1L);
    usuario.setNombre("Juan");
    usuario.setApellido("Perez");
    usuario.setEdad(30);
    usuario.setDeporte(Deporte.TENIS);
    return usuario;
  }

  private DatosPerfil unosDatos(MultipartFile foto) {
    DatosPerfil datos = new DatosPerfil();
    datos.setNombre("Juan");
    datos.setApellido("Perez");
    datos.setEdad(30);
    datos.setDeporte(Deporte.TENIS);
    datos.setFoto(foto);
    return datos;
  }
}
