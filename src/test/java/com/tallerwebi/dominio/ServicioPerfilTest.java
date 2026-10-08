package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tallerwebi.dominio.enums.Deporte;
import com.tallerwebi.dominio.excepcion.EdadInvalida;
import com.tallerwebi.dominio.excepcion.FotoDemasiadoGrande;
import com.tallerwebi.dominio.excepcion.FotoInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioPerfil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioPerfilTest {

  private static final int DOS_MB = 2 * 1024 * 1024;

  private RepositorioUsuario repoUsuarioMock;
  private ServicioPerfil servicio;

  @BeforeEach
  public void init() {
    repoUsuarioMock = mock(RepositorioUsuario.class);
    servicio = new ServicioPerfilImpl(repoUsuarioMock);
  }

  // ---------- obtener ----------

  @Test
  public void deberiaObtenerElUsuarioPorSuId() throws Exception {
    Usuario usuario = dadoQueExisteUnUsuario(1L, "SOCIO");

    Usuario resultado = servicio.obtener(1L);

    assertThat(resultado, equalTo(usuario));
  }

  @Test
  public void noDeberiaObtenerUnUsuarioQueNoExiste() throws Exception {
    when(repoUsuarioMock.buscarPorId(99L)).thenReturn(null);

    assertThrows(UsuarioNoEncontrado.class, () -> servicio.obtener(99L));
  }

  // ---------- actualizar: datos ----------

  @Test
  public void deberiaCopiarLosDatosEditablesYGuardar() throws Exception {
    Usuario usuario = dadoQueExisteUnUsuario(1L, "SOCIO");
    Usuario cambios = new Usuario();
    cambios.setNombre("Juan");
    cambios.setApellido("Perez");
    cambios.setEdad(30);
    cambios.setDeporte(Deporte.PADEL);

    servicio.actualizar(1L, cambios, null, null);

    assertThat(usuario.getNombre(), equalTo("Juan"));
    assertThat(usuario.getApellido(), equalTo("Perez"));
    assertThat(usuario.getEdad(), equalTo(30));
    assertThat(usuario.getDeporte(), equalTo(Deporte.PADEL));
    verify(repoUsuarioMock, times(1)).modificar(usuario);
  }

  @Test
  public void deberiaGuardarAunqueNingunCampoTengaValor() throws Exception {
    Usuario usuario = dadoQueExisteUnUsuario(1L, "SOCIO");

    servicio.actualizar(1L, new Usuario(), null, null);

    assertThat(usuario.getEdad(), nullValue());
    verify(repoUsuarioMock, times(1)).modificar(usuario);
  }

  @Test
  public void noDeberiaCambiarElRolNiLasCredencialesDelUsuario() throws Exception {
    Usuario usuario = dadoQueExisteUnUsuario(1L, "SOCIO");
    usuario.setEmail("socio@unlam.edu.ar");
    usuario.setPassword("test");
    Usuario cambios = new Usuario();
    cambios.setRol("ADMIN");
    cambios.setEmail("otro@correo.com");
    cambios.setPassword("hackeada");

    servicio.actualizar(1L, cambios, null, null);

    assertThat(usuario.getRol(), equalTo("SOCIO"));
    assertThat(usuario.getEmail(), equalTo("socio@unlam.edu.ar"));
    assertThat(usuario.getPassword(), equalTo("test"));
  }

  @Test
  public void noDeberiaActualizarUnUsuarioQueNoExiste() throws Exception {
    when(repoUsuarioMock.buscarPorId(99L)).thenReturn(null);

    assertThrows(
      UsuarioNoEncontrado.class,
      () -> servicio.actualizar(99L, new Usuario(), null, null)
    );
    verify(repoUsuarioMock, never()).modificar(any(Usuario.class));
  }

  // ---------- actualizar: deporte ----------

  @Test
  public void elSocioDeberiaPoderCambiarSuDeporte() throws Exception {
    Usuario usuario = dadoQueExisteUnUsuario(1L, "SOCIO");
    usuario.setDeporte(Deporte.TENIS);
    Usuario cambios = new Usuario();
    cambios.setDeporte(Deporte.BOXEO);

    servicio.actualizar(1L, cambios, null, null);

    assertThat(usuario.getDeporte(), equalTo(Deporte.BOXEO));
  }

  @Test
  public void elEntrenadorNoDeberiaCambiarElDeporte() throws Exception {
    Usuario usuario = dadoQueExisteUnUsuario(2L, "ENTRENADOR");
    usuario.setDeporte(null);
    Usuario cambios = new Usuario();
    cambios.setDeporte(Deporte.BOXEO);

    servicio.actualizar(2L, cambios, null, null);

    assertThat(usuario.getDeporte(), nullValue());
  }

  // ---------- actualizar: edad ----------

  @Test
  public void noDeberiaAceptarUnaEdadMenorAUno() throws Exception {
    dadoQueExisteUnUsuario(1L, "SOCIO");
    Usuario cambios = new Usuario();
    cambios.setEdad(0);

    assertThrows(EdadInvalida.class, () -> servicio.actualizar(1L, cambios, null, null));
    verify(repoUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void noDeberiaAceptarUnaEdadMayorA120() throws Exception {
    dadoQueExisteUnUsuario(1L, "SOCIO");
    Usuario cambios = new Usuario();
    cambios.setEdad(121);

    assertThrows(EdadInvalida.class, () -> servicio.actualizar(1L, cambios, null, null));
    verify(repoUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void deberiaAceptarLasEdadesLimite() throws Exception {
    Usuario usuario = dadoQueExisteUnUsuario(1L, "SOCIO");
    Usuario minima = new Usuario();
    minima.setEdad(1);
    Usuario maxima = new Usuario();
    maxima.setEdad(120);

    servicio.actualizar(1L, minima, null, null);
    assertThat(usuario.getEdad(), equalTo(1));
    servicio.actualizar(1L, maxima, null, null);
    assertThat(usuario.getEdad(), equalTo(120));
  }

  // ---------- actualizar: foto ----------

  @Test
  public void deberiaGuardarLaFotoComoTextoBase64() throws Exception {
    Usuario usuario = dadoQueExisteUnUsuario(1L, "SOCIO");
    byte[] foto = { 1, 2, 3 };

    servicio.actualizar(1L, new Usuario(), foto, "image/jpeg");

    assertThat(usuario.getFotoPerfil(), startsWith("data:image/jpeg;base64,"));
    assertThat(usuario.getFotoPerfil(), equalTo("data:image/jpeg;base64,AQID"));
  }

  @Test
  public void deberiaAceptarUnaFotoDeExactamenteDosMegas() throws Exception {
    Usuario usuario = dadoQueExisteUnUsuario(1L, "SOCIO");

    servicio.actualizar(1L, new Usuario(), new byte[DOS_MB], "image/png");

    assertThat(usuario.getFotoPerfil(), startsWith("data:image/png;base64,"));
  }

  @Test
  public void noDeberiaAceptarUnaFotoDeMasDeDosMegas() throws Exception {
    dadoQueExisteUnUsuario(1L, "SOCIO");
    byte[] demasiadoGrande = new byte[DOS_MB + 1];

    assertThrows(
      FotoDemasiadoGrande.class,
      () -> servicio.actualizar(1L, new Usuario(), demasiadoGrande, "image/jpeg")
    );
    verify(repoUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void noDeberiaAceptarUnaFotoDeUnTipoNoPermitido() throws Exception {
    dadoQueExisteUnUsuario(1L, "SOCIO");

    assertThrows(
      FotoInvalida.class,
      () -> servicio.actualizar(1L, new Usuario(), new byte[] { 1, 2, 3 }, "image/gif")
    );
    verify(repoUsuarioMock, never()).modificar(any(Usuario.class));
  }

  @Test
  public void noDeberiaAceptarUnaFotoSinTipo() throws Exception {
    dadoQueExisteUnUsuario(1L, "SOCIO");

    assertThrows(
      FotoInvalida.class,
      () -> servicio.actualizar(1L, new Usuario(), new byte[] { 1, 2, 3 }, null)
    );
  }

  @Test
  public void noDeberiaPisarLaFotoActualSiNoLlegaUnaNueva() throws Exception {
    Usuario usuario = dadoQueExisteUnUsuario(1L, "SOCIO");
    usuario.setFotoPerfil("data:image/png;base64,AAAA");

    servicio.actualizar(1L, new Usuario(), null, null);

    assertThat(usuario.getFotoPerfil(), equalTo("data:image/png;base64,AAAA"));
  }

  @Test
  public void noDeberiaPisarLaFotoActualSiLaNuevaVieneVacia() throws Exception {
    Usuario usuario = dadoQueExisteUnUsuario(1L, "SOCIO");
    usuario.setFotoPerfil("data:image/png;base64,AAAA");

    servicio.actualizar(1L, new Usuario(), new byte[0], "application/octet-stream");

    assertThat(usuario.getFotoPerfil(), equalTo("data:image/png;base64,AAAA"));
    verify(repoUsuarioMock, times(1)).modificar(usuario);
  }

  @Test
  public void noDeberiaModificarNadaSiLaFotoEsInvalida() throws Exception {
    Usuario usuario = dadoQueExisteUnUsuario(1L, "SOCIO");
    usuario.setNombre("Original");
    Usuario cambios = new Usuario();
    cambios.setNombre("Nuevo");

    assertThrows(
      FotoInvalida.class,
      () -> servicio.actualizar(1L, cambios, new byte[] { 1 }, "text/plain")
    );

    assertThat(usuario.getNombre(), equalTo("Original"));
  }

  // ---------- ayudas ----------

  private Usuario dadoQueExisteUnUsuario(Long id, String rol) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    usuario.setRol(rol);
    when(repoUsuarioMock.buscarPorId(id)).thenReturn(usuario);
    return usuario;
  }
}
