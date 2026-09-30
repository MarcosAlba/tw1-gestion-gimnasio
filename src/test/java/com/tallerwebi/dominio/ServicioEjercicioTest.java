package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.EjercicioExistente;
import com.tallerwebi.dominio.excepcion.NombreEjercicioInvalido;
import com.tallerwebi.dominio.interfaces.RepositorioEjercicio;
import com.tallerwebi.dominio.interfaces.ServicioEjercicio;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServicioEjercicioTest {

  private RepositorioEjercicio repoMock;
  private ServicioEjercicio servicio;

  @BeforeEach
  public void init() {
    repoMock = mock(RepositorioEjercicio.class);
    servicio = new ServicioEjercicioImpl(repoMock);
  }

  @Test
  public void deberiaRegistrarUnEjercicioConNombreNuevo()
    throws EjercicioExistente, NombreEjercicioInvalido {
    Ejercicio ejercicio = new Ejercicio();
    ejercicio.setNombre("Sentadilla");
    when(repoMock.buscarPorNombre("Sentadilla")).thenReturn(null);

    servicio.registrar(ejercicio);

    verify(repoMock, times(1)).guardar(ejercicio);
  }

  @Test
  public void noDeberiaRegistrarUnEjercicioConNombreNulo() {
    Ejercicio ejercicio = new Ejercicio();

    assertThrows(NombreEjercicioInvalido.class, () -> servicio.registrar(ejercicio));
    verify(repoMock, never()).guardar(any());
  }

  @Test
  public void noDeberiaRegistrarUnEjercicioConNombreVacio() {
    Ejercicio ejercicio = new Ejercicio();
    ejercicio.setNombre("");

    assertThrows(NombreEjercicioInvalido.class, () -> servicio.registrar(ejercicio));
    verify(repoMock, never()).guardar(any());
  }

  @Test
  public void noDeberiaRegistrarUnEjercicioConNombreSoloConEspacios() {
    Ejercicio ejercicio = new Ejercicio();
    ejercicio.setNombre("      ");

    assertThrows(NombreEjercicioInvalido.class, () -> servicio.registrar(ejercicio));
    verify(repoMock, never()).guardar(any());
  }

  @Test
  public void noDeberiaRegistrarUnEjercicioConNombreRepetido() {
    Ejercicio ejercicio = new Ejercicio();
    ejercicio.setNombre("Burpees");
    Ejercicio ejercicio1 = new Ejercicio();
    ejercicio1.setNombre("Burpees");

    when(repoMock.buscarPorNombre("Burpees")).thenReturn(ejercicio);

    assertThrows(EjercicioExistente.class, () -> servicio.registrar(ejercicio1));

    verify(repoMock, never()).guardar(any());
  }

  @Test
  public void deberiaListarTodosLosEjercicios() {
    Ejercicio sentadilla = new Ejercicio();
    sentadilla.setNombre("Sentadilla");
    Ejercicio burpees = new Ejercicio();
    burpees.setNombre("Burpees");
    List<Ejercicio> ejercicios = List.of(sentadilla, burpees);
    when(repoMock.buscarTodos()).thenReturn(ejercicios);

    List<Ejercicio> resultado = servicio.listarTodos();

    assertThat(resultado, equalTo(ejercicios));
  }
}
