package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.Ejercicio;
import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.enums.NivelDificultad;
import com.tallerwebi.dominio.interfaces.RepositorioEjercicio;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { HibernateInfraestructuraTestConfig.class })
public class RepositorioEjercicioTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioEjercicio repoEjercicio;

  @BeforeEach
  public void init() {
    repoEjercicio = new RepositorioEjercicioImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnEjercicio() {
    Ejercicio sentadilla = dadoQueTengoUnEjercicio("Sentadilla", CapacidadFisica.FUERZA);
    repoEjercicio.guardar(sentadilla);
    Ejercicio obtenido = repoEjercicio.buscarPorNombre("Sentadilla");

    assertThat(obtenido.getId(), notNullValue());
    assertThat(obtenido.getNombre(), equalTo("Sentadilla"));
    assertThat(obtenido.getDescripcion(), equalTo("Descripcion de Sentadilla"));
    assertThat(obtenido.getCapacidad(), equalTo(CapacidadFisica.FUERZA));
    assertThat(obtenido.getDificultad(), equalTo(NivelDificultad.INTERMEDIO));
  }

  @Test
  @Transactional
  public void noDeberiaEncontrarUnEjercicioInexistentePorNombre() {
    Ejercicio resultado = repoEjercicio.buscarPorNombre("Inexistente");
    assertThat(resultado, is(nullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarTodosLosEjercicios() {
    Ejercicio sentadilla = dadoQueTengoUnEjercicio("Sentadilla", CapacidadFisica.FUERZA);
    Ejercicio Burpees = dadoQueTengoUnEjercicio("Burpees", CapacidadFisica.CARDIO);
    repoEjercicio.guardar(sentadilla);
    repoEjercicio.guardar(Burpees);

    List<Ejercicio> lista = repoEjercicio.buscarTodos();
    assertThat(lista, hasSize(2));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarSoloLosEjerciciosDeUnaCapacidad() {
    Ejercicio sentadilla = dadoQueTengoUnEjercicio("Sentadilla", CapacidadFisica.FUERZA);
    Ejercicio zigzag = dadoQueTengoUnEjercicio("Zigzag entre conos", CapacidadFisica.AGILIDAD);
    Ejercicio desplazamientos = dadoQueTengoUnEjercicio(
      "Desplazamientos laterales",
      CapacidadFisica.AGILIDAD
    );
    repoEjercicio.guardar(sentadilla);
    repoEjercicio.guardar(zigzag);
    repoEjercicio.guardar(desplazamientos);

    List<Ejercicio> ejerciciosConAgilidad = repoEjercicio.buscarPorCapacidad(
      CapacidadFisica.AGILIDAD
    );
    assertThat(ejerciciosConAgilidad, hasSize(2));
    for (Ejercicio e : ejerciciosConAgilidad) {
      assertThat(e.getCapacidad(), equalTo(CapacidadFisica.AGILIDAD));
    }
  }

  private Ejercicio dadoQueTengoUnEjercicio(String nombre, CapacidadFisica capacidad) {
    Ejercicio ejercicio = new Ejercicio();
    ejercicio.setNombre(nombre);
    ejercicio.setDescripcion("Descripcion de " + nombre);
    ejercicio.setCapacidad(capacidad);
    ejercicio.setDificultad(NivelDificultad.INTERMEDIO);
    return ejercicio;
  }
}
