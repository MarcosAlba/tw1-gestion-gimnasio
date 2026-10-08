package com.tallerwebi.dominio;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.enums.TipoMembresia;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.interfaces.RepositorioMembresia;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioMembresia;
import java.time.LocalDate;
import java.util.List;
import net.bytebuddy.matcher.ElementMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ServicioMembresiaTest {

  private RepositorioMembresia repoMembresiaMock;
  private RepositorioUsuario repoUsuarioMock;
  private ServicioMembresia servicio;

  @BeforeEach
  public void init() {
    repoMembresiaMock = mock(RepositorioMembresia.class);
    repoUsuarioMock = mock(RepositorioUsuario.class);
    servicio = new ServicioMembresiaImpl(repoMembresiaMock, repoUsuarioMock);
  }

  @Test
  public void deberiaRegistrarUnaMembresiaConInicioHoyYVencimientoCalculado() {
    Usuario socio = new Usuario();
    socio.setId(1L);
    when(repoUsuarioMock.buscarPorId(1L)).thenReturn(socio);

    servicio.registrar(1L, TipoMembresia.TRIMESTRAL);

    ArgumentCaptor<Membresia> captor = ArgumentCaptor.forClass(Membresia.class);
    verify(repoMembresiaMock, times(1)).guardar(captor.capture());
    Membresia guardada = captor.getValue();
    assertThat(guardada.getSocio(), equalTo(socio));
    assertThat(guardada.getTipo(), equalTo(TipoMembresia.TRIMESTRAL));
    assertThat(guardada.getFechaInicio(), equalTo(LocalDate.now()));
    assertThat(guardada.getFechaVencimiento(), equalTo(LocalDate.now().plusMonths(3)));
  }

  @Test
  public void noDeberiaRegistrarUnaMembresiaSiElSocioNoExiste() {
    when(repoUsuarioMock.buscarPorId(99L)).thenReturn(null);

    assertThrows(UsuarioNoEncontrado.class, () -> servicio.registrar(99L, TipoMembresia.MENSUAL));
    verify(repoMembresiaMock, never()).guardar(any(Membresia.class));
  }

  @Test
  public void deberiaObtenerElHistorialDeMembresiasDelSocio() {
    List<Membresia> membresias = List.of(new Membresia(), new Membresia());
    when(repoMembresiaMock.buscarPorSocio(1L)).thenReturn(membresias);

    List<Membresia> resultado = servicio.historial(1L);

    assertThat(resultado, equalTo(membresias));
  }

  @Test
  public void deberiaObtenerLaMembresiaVigenteDelSocio() {
    Membresia vigente = new Membresia();
    when(repoMembresiaMock.buscarVigente(1L, LocalDate.now())).thenReturn(vigente);

    Membresia resultado = servicio.obtenerVigente(1L);

    assertThat(resultado, equalTo(vigente));
  }

  @Test
  public void deberiaCalcularLosDiasRestantesDeLaMembresiaVigente() {
    LocalDate hoy = LocalDate.now();
    Membresia vigente = new Membresia();
    vigente.setFechaVencimiento(hoy.plusDays(4));
    when(repoMembresiaMock.buscarVigente(1L, hoy)).thenReturn(vigente);

    Long dias = servicio.obtenerDiasRestantes(1L);

    assertThat(dias, equalTo(4L));
  }

  @Test
  public void deberiaRetornarNullSiElSocioNoTieneMembresiaVigente() {
    when(repoMembresiaMock.buscarVigente(eq(1L), any(LocalDate.class))).thenReturn(null);

    Long dias = servicio.obtenerDiasRestantes(1L);

    assertNull(dias);
  public void deberiaDevolverHistorialVacioSiElSocioNoTieneMembresias() {
    when(repoMembresiaMock.buscarPorSocio(1L)).thenReturn(List.of());
    List<Membresia> resultado = servicio.historial(1L);
    assertThat(resultado, equalTo(List.of()));
  }
}
