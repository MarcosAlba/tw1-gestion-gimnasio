package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.EntrenadorInvalido;
import com.tallerwebi.dominio.interfaces.RepositorioClase;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioClase;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioClaseTest {

  private RepositorioClase repoClaseMock;
  private RepositorioUsuario repoUsuarioMock;
  private ServicioClase servicio;

  @BeforeEach
  public void init() {
    repoClaseMock = mock(RepositorioClase.class);
    repoUsuarioMock = mock(RepositorioUsuario.class);
    servicio = new ServicioClaseImpl(repoClaseMock, repoUsuarioMock);
  }

  @Test
  public void deberiaCrearUnaClaseAsignandoleElEntrenador() throws Exception {
    Usuario entrenador = dadoQueExisteUnUsuarioConRol(1L, "ENTRENADOR");
    Clase clase = new Clase();

    servicio.crear(clase, 1L);

    assertThat(clase.getEntrenador(), equalTo(entrenador));
    verify(repoClaseMock, times(1)).guardar(clase);
  }

  @Test
  public void noDeberiaCrearUnaClaseSiElUsuarioEsSocio() {
    dadoQueExisteUnUsuarioConRol(2L, "SOCIO");
    Clase clase = new Clase();

    assertThrows(EntrenadorInvalido.class, () -> servicio.crear(clase, 2L));
    verify(repoClaseMock, never()).guardar(any(Clase.class));
  }

  @Test
  public void noDeberiaCrearUnaClaseSiElEntrenadorNoExiste() {
    when(repoUsuarioMock.buscarPorId(99L)).thenReturn(null);
    Clase clase = new Clase();

    assertThrows(EntrenadorInvalido.class, () -> servicio.crear(clase, 99L));
    verify(repoClaseMock, never()).guardar(any(Clase.class));
  }

  @Test
  public void deberiaListarLasProximasClases() {
    List<Clase> clases = List.of(new Clase(), new Clase());
    when(repoClaseMock.buscarDesde(any(LocalDateTime.class))).thenReturn(clases);

    List<Clase> resultado = servicio.listarProximas();

    assertThat(resultado, equalTo(clases));
  }

  private Usuario dadoQueExisteUnUsuarioConRol(Long id, String rol) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    usuario.setRol(rol);
    when(repoUsuarioMock.buscarPorId(id)).thenReturn(usuario);
    return usuario;
  }
}
