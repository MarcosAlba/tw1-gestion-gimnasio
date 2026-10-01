package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.enums.Deporte;
import com.tallerwebi.dominio.excepcion.SocioSinDeporte;
import com.tallerwebi.dominio.interfaces.RepositorioEjercicio;
import com.tallerwebi.dominio.interfaces.RepositorioRutina;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioRutina;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioRutinaTest {

  private RepositorioRutina repoRutinaMock;
  private RepositorioEjercicio repoEjercicioMock;
  private RepositorioUsuario repoUsuarioMock;
  private ServicioRutina servicio;

  @BeforeEach
  public void init() {
    repoRutinaMock = mock(RepositorioRutina.class);
    repoEjercicioMock = mock(RepositorioEjercicio.class);
    repoUsuarioMock = mock(RepositorioUsuario.class);
    servicio = new ServicioRutinaImpl(repoRutinaMock, repoEjercicioMock, repoUsuarioMock);
  }

  @Test
  public void deberiaGenerarUnaRutinaDeTenisConLaCantidadDeEjerciciosPorCapacidad()
    throws SocioSinDeporte {
    dadoQueExisteUnSocioConDeporte(1L, Deporte.TENIS);
    dadoQueHayEjerciciosDe(CapacidadFisica.AGILIDAD, 10);
    dadoQueHayEjerciciosDe(CapacidadFisica.CARDIO, 10);
    dadoQueHayEjerciciosDe(CapacidadFisica.COORDINACION, 10);
    dadoQueHayEjerciciosDe(CapacidadFisica.FUERZA, 10);

    Rutina rutina = servicio.generar(1L);

    assertThat(rutina.getEjercicios(), hasSize(8));
    assertThat(contar(rutina, CapacidadFisica.AGILIDAD), equalTo(3L));
    assertThat(contar(rutina, CapacidadFisica.CARDIO), equalTo(2L));
    assertThat(contar(rutina, CapacidadFisica.COORDINACION), equalTo(2L));
    assertThat(contar(rutina, CapacidadFisica.FUERZA), equalTo(1L));
  }

  @Test
  public void deberiaCompletarLosDatosDeLaRutinaYGuardarla() throws SocioSinDeporte {
    dadoQueExisteUnSocioConDeporte(1L, Deporte.TENIS);
    dadoQueHayEjerciciosDe(CapacidadFisica.AGILIDAD, 3);

    Rutina rutina = servicio.generar(1L);

    assertThat(rutina.getSocio().getId(), equalTo(1L));
    assertThat(rutina.getDeporte(), equalTo(Deporte.TENIS));
    assertThat(rutina.getFechaCreacion(), notNullValue());
    verify(repoRutinaMock, times(1)).guardar(rutina);
  }

  @Test
  public void deberiaTomarSoloLosEjerciciosDisponiblesSiHayMenosDeLosPedidos() throws Exception {
    dadoQueExisteUnSocioConDeporte(1L, Deporte.TENIS);
    dadoQueHayEjerciciosDe(CapacidadFisica.AGILIDAD, 1);
    dadoQueHayEjerciciosDe(CapacidadFisica.CARDIO, 3);
    dadoQueHayEjerciciosDe(CapacidadFisica.COORDINACION, 3);
    dadoQueHayEjerciciosDe(CapacidadFisica.FUERZA, 3);

    Rutina rutina = servicio.generar(1L);

    assertThat(contar(rutina, CapacidadFisica.AGILIDAD), equalTo(1L));
    assertThat(rutina.getEjercicios(), hasSize(6));
  }

  @Test
  public void noDeberiaGenerarUnaRutinaSiElSocioNoTieneDeporte() {
    dadoQueExisteUnSocioConDeporte(1L, null);

    assertThrows(SocioSinDeporte.class, () -> servicio.generar(1L));
    verify(repoRutinaMock, never()).guardar(any(Rutina.class));
  }

  @Test
  public void noDeberiaGenerarUnaRutinaSiElSocioNoExiste() {
    when(repoUsuarioMock.buscarPorId(99L)).thenReturn(null);

    assertThrows(SocioSinDeporte.class, () -> servicio.generar(99L));
    verify(repoRutinaMock, never()).guardar(any(Rutina.class));
  }

  @Test
  public void deberiaObtenerLaUltimaRutinaDelSocio() {
    Rutina ultima = new Rutina();
    when(repoRutinaMock.buscarUltimaDeSocio(1L)).thenReturn(ultima);

    Rutina obtenida = servicio.obtenerUltima(1L);

    assertThat(obtenida, equalTo(ultima));
  }

  private void dadoQueExisteUnSocioConDeporte(Long id, Deporte deporte) {
    Usuario socio = new Usuario();
    socio.setId(id);
    socio.setDeporte(deporte);
    when(repoUsuarioMock.buscarPorId(id)).thenReturn(socio);
  }

  private void dadoQueHayEjerciciosDe(CapacidadFisica capacidad, int cantidad) {
    List<Ejercicio> ejercicios = new ArrayList<>();
    for (int i = 0; i < cantidad; i++) {
      Ejercicio ejercicio = new Ejercicio();
      ejercicio.setNombre(capacidad + " " + i);
      ejercicio.setCapacidad(capacidad);
      ejercicios.add(ejercicio);
    }
    when(repoEjercicioMock.buscarPorCapacidad(capacidad)).thenReturn(ejercicios);
  }

  private long contar(Rutina rutina, CapacidadFisica capacidad) {
    return rutina.getEjercicios().stream().filter(e -> e.getCapacidad() == capacidad).count();
  }
}
