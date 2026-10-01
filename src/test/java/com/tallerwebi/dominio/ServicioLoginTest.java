package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.enums.Deporte;
import com.tallerwebi.dominio.excepcion.RolInvalido;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioLogin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioLoginTest {

  private ServicioLogin servicioLogin;
  private RepositorioUsuario repositorioUsuarioMock;

  @BeforeEach
  public void init() {
    this.repositorioUsuarioMock = mock(RepositorioUsuario.class);
    this.servicioLogin = new ServicioLoginImpl(this.repositorioUsuarioMock);
  }

  @Test
  public void consultarUsuarioDeberiaLlamarAlRepositorio() {
    // preparacion
    String email = "test@test.com";
    String password = "password";
    Usuario usuarioEsperado = new Usuario();
    when(this.repositorioUsuarioMock.buscarUsuario(email, password)).thenReturn(usuarioEsperado);

    // ejecucion
    Usuario usuarioObtenido = this.servicioLogin.consultarUsuario(email, password);

    // validacion
    assertThat(usuarioObtenido, equalTo(usuarioEsperado));
    verify(this.repositorioUsuarioMock, times(1)).buscarUsuario(email, password);
  }

  @Test
  public void registrarUsuarioSiNoExisteDeberiaGuardarlo() throws Exception {
    // preparacion
    Usuario usuario = dadoQueTengoUnUsuarioConRol("nuevo@test.com", "SOCIO");
    when(this.repositorioUsuarioMock.buscar(usuario.getEmail())).thenReturn(null);

    // ejecucion
    this.servicioLogin.registrar(usuario);

    // validacion
    verify(this.repositorioUsuarioMock, times(1)).guardar(usuario);
  }

  @Test
  public void registrarUsuarioSiExisteDeberiaLanzarExcepcion() {
    // preparacion
    Usuario usuario = dadoQueTengoUnUsuarioConRol("existe@test.com", "SOCIO");
    when(this.repositorioUsuarioMock.buscar(usuario.getEmail())).thenReturn(new Usuario());

    // ejecucion y validacion
    assertThrows(UsuarioExistente.class, () -> this.servicioLogin.registrar(usuario));
    verify(this.repositorioUsuarioMock, times(0)).guardar(usuario);
  }

  @Test
  public void registrarDeberiaChequearElDuplicadoSoloPorEmail() {
    // Con el mismo email y otra contraseña, el usuario igual ya existe
    Usuario usuario = dadoQueTengoUnUsuarioConRol("existe@test.com", "SOCIO");
    usuario.setPassword("otra-clave");
    when(this.repositorioUsuarioMock.buscar("existe@test.com")).thenReturn(new Usuario());

    assertThrows(UsuarioExistente.class, () -> this.servicioLogin.registrar(usuario));
    verify(this.repositorioUsuarioMock, never()).buscarUsuario(anyString(), anyString());
  }

  @Test
  public void registrarUnEntrenadorDeberiaGuardarloConEseRol() throws Exception {
    Usuario usuario = dadoQueTengoUnUsuarioConRol("entrenador@test.com", "ENTRENADOR");

    this.servicioLogin.registrar(usuario);

    assertThat(usuario.getRol(), equalTo("ENTRENADOR"));
    verify(this.repositorioUsuarioMock, times(1)).guardar(usuario);
  }

  @Test
  public void registrarUnEntrenadorDeberiaIgnorarElDeporte() throws Exception {
    Usuario usuario = dadoQueTengoUnUsuarioConRol("entrenador@test.com", "ENTRENADOR");
    usuario.setDeporte(Deporte.TENIS);

    this.servicioLogin.registrar(usuario);

    assertThat(usuario.getDeporte(), equalTo(null));
  }

  @Test
  public void registrarUnSocioDeberiaConservarSuDeporte() throws Exception {
    Usuario usuario = dadoQueTengoUnUsuarioConRol("socio@test.com", "SOCIO");
    usuario.setDeporte(Deporte.TENIS);

    this.servicioLogin.registrar(usuario);

    assertThat(usuario.getDeporte(), equalTo(Deporte.TENIS));
  }

  @Test
  public void registrarConUnRolNoPermitidoDeberiaLanzarRolInvalido() {
    Usuario usuario = dadoQueTengoUnUsuarioConRol("admin@test.com", "ADMIN");

    assertThrows(RolInvalido.class, () -> this.servicioLogin.registrar(usuario));
    verify(this.repositorioUsuarioMock, never()).guardar(any(Usuario.class));
  }

  @Test
  public void registrarSinRolDeberiaLanzarRolInvalido() {
    Usuario usuario = dadoQueTengoUnUsuarioConRol("sinrol@test.com", null);

    assertThrows(RolInvalido.class, () -> this.servicioLogin.registrar(usuario));
    verify(this.repositorioUsuarioMock, never()).guardar(any(Usuario.class));
  }

  private Usuario dadoQueTengoUnUsuarioConRol(String email, String rol) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword("123");
    usuario.setRol(rol);
    return usuario;
  }
}
