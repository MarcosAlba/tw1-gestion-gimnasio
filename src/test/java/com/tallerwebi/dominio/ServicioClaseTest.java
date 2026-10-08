package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.excepcion.EntrenadorInvalido;
import com.tallerwebi.dominio.excepcion.FechaClaseInvalida;
import com.tallerwebi.dominio.interfaces.RepositorioClase;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioClase;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    clase.setInicio(LocalDateTime.now().plusDays(1));

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

  @Test
  public void deberiaAgruparLasClasesPorDiaIncluyendoLosDiasSinClases() {
    LocalDate hoy = LocalDate.now();
    Clase deHoy = unaClase("Spinning", hoy.atTime(18, 0), CapacidadFisica.CARDIO);
    Clase dentroDeTres = unaClase(
      "Funcional",
      hoy.plusDays(3).atTime(10, 0),
      CapacidadFisica.FUERZA
    );
    when(repoClaseMock.buscarDesde(any(LocalDateTime.class)))
      .thenReturn(List.of(deHoy, dentroDeTres));

    Map<LocalDate, List<Clase>> semana = servicio.listarSemana(null);

    assertThat(semana.size(), equalTo(7));
    assertThat(semana.get(hoy), equalTo(List.of(deHoy)));
    assertThat(semana.get(hoy.plusDays(3)), equalTo(List.of(dentroDeTres)));
    assertThat(semana.get(hoy.plusDays(1)).isEmpty(), equalTo(true));
  }

  @Test
  public void deberiaDejarSoloLasClasesDeLaCapacidadPedida() {
    LocalDate hoy = LocalDate.now();
    Clase cardio = unaClase("Spinning", hoy.atTime(18, 0), CapacidadFisica.CARDIO);
    Clase fuerza = unaClase("Funcional", hoy.atTime(19, 0), CapacidadFisica.FUERZA);
    when(repoClaseMock.buscarDesde(any(LocalDateTime.class))).thenReturn(List.of(cardio, fuerza));

    Map<LocalDate, List<Clase>> semana = servicio.listarSemana(CapacidadFisica.CARDIO);

    assertThat(semana.get(hoy), equalTo(List.of(cardio)));
  }

  @Test
  public void deberiaOrdenarLasClasesDeCadaDiaPorHora() {
    LocalDate hoy = LocalDate.now();
    Clase tarde = unaClase("Spinning", hoy.atTime(20, 0), CapacidadFisica.CARDIO);
    Clase temprano = unaClase("Funcional", hoy.atTime(8, 0), CapacidadFisica.FUERZA);
    when(repoClaseMock.buscarDesde(any(LocalDateTime.class))).thenReturn(List.of(tarde, temprano));

    Map<LocalDate, List<Clase>> semana = servicio.listarSemana(null);

    assertThat(semana.get(hoy), equalTo(List.of(temprano, tarde)));
  }

  @Test
  public void noDeberiaIncluirLasClasesQueEstanFueraDeLaSemana() {
    LocalDate hoy = LocalDate.now();
    Clase lejana = unaClase("Spinning", hoy.plusDays(10).atTime(18, 0), CapacidadFisica.CARDIO);
    when(repoClaseMock.buscarDesde(any(LocalDateTime.class))).thenReturn(List.of(lejana));

    Map<LocalDate, List<Clase>> semana = servicio.listarSemana(null);

    assertThat(semana.size(), equalTo(7));
    assertThat(semana.values().stream().allMatch(List::isEmpty), equalTo(true));
  }

  @Test
  public void noDeberiaCrearUnaClaseSiLaFechaYaPaso() {
    dadoQueExisteUnUsuarioConRol(1L, "ENTRENADOR");
    Clase clase = new Clase();
    clase.setInicio(LocalDateTime.now().minusDays(1));

    assertThrows(FechaClaseInvalida.class, () -> servicio.crear(clase, 1L));
    verify(repoClaseMock, never()).guardar(any(Clase.class));
  }

  private Usuario dadoQueExisteUnUsuarioConRol(Long id, String rol) {
    Usuario usuario = new Usuario();
    usuario.setId(id);
    usuario.setRol(rol);
    when(repoUsuarioMock.buscarPorId(id)).thenReturn(usuario);
    return usuario;
  }

  private Clase unaClase(String nombre, LocalDateTime inicio, CapacidadFisica capacidad) {
    Clase clase = new Clase();
    clase.setNombre(nombre);
    clase.setInicio(inicio);
    clase.setCapacidad(capacidad);
    return clase;
  }
}
