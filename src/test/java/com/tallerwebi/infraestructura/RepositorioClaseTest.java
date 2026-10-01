package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.Clase;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.interfaces.RepositorioClase;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
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
public class RepositorioClaseTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioClase repoClase;

  @BeforeEach
  public void init() {
    repoClase = new RepositorioClaseImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnaClaseYBuscarlaPorId() {
    Usuario entrenador = dadoQueExisteUnEntrenador("entrenador@test.com");
    LocalDateTime inicio = LocalDateTime.now().plusDays(1);
    Clase clase = dadoQueTengoUnaClase(entrenador, "Spinning", inicio);

    repoClase.guardar(clase);
    Clase obtenida = repoClase.buscarPorId(clase.getId());

    assertThat(obtenida.getId(), notNullValue());
    assertThat(obtenida.getNombre(), equalTo("Spinning"));
    assertThat(obtenida.getInicio(), equalTo(inicio));
    assertThat(obtenida.getDuracion(), equalTo(45));
    assertThat(obtenida.getLugar(), equalTo("Sala 2"));
    assertThat(obtenida.getCupo(), equalTo(15));
    assertThat(obtenida.getCapacidad(), equalTo(CapacidadFisica.CARDIO));
    assertThat(obtenida.getEntrenador(), equalTo(entrenador));
  }

  @Test
  @Transactional
  public void noDeberiaEncontrarUnaClaseInexistentePorId() {
    Clase obtenida = repoClase.buscarPorId(999L);

    assertThat(obtenida, is(nullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaBuscarLasClasesQueYaPasaron() {
    Usuario entrenador = dadoQueExisteUnEntrenador("entrenador@test.com");
    repoClase.guardar(dadoQueTengoUnaClase(entrenador, "Pasada", LocalDateTime.now().minusDays(1)));
    Clase futura = dadoQueTengoUnaClase(entrenador, "Futura", LocalDateTime.now().plusDays(1));
    repoClase.guardar(futura);

    List<Clase> clases = repoClase.buscarDesde(LocalDateTime.now());

    assertThat(clases, hasSize(1));
    assertThat(clases.get(0), equalTo(futura));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarLasClasesOrdenadasPorFechaDeInicio() {
    Usuario entrenador = dadoQueExisteUnEntrenador("entrenador@test.com");
    Clase tardia = dadoQueTengoUnaClase(entrenador, "Tardia", LocalDateTime.now().plusDays(3));
    Clase temprana = dadoQueTengoUnaClase(entrenador, "Temprana", LocalDateTime.now().plusDays(1));
    repoClase.guardar(tardia);
    repoClase.guardar(temprana);

    List<Clase> clases = repoClase.buscarDesde(LocalDateTime.now());

    assertThat(clases, hasSize(2));
    assertThat(clases.get(0), equalTo(temprana));
    assertThat(clases.get(1), equalTo(tardia));
  }

  private Usuario dadoQueExisteUnEntrenador(String email) {
    Usuario entrenador = new Usuario();
    entrenador.setEmail(email);
    entrenador.setPassword("123");
    entrenador.setRol("ENTRENADOR");
    sessionFactory.getCurrentSession().persist(entrenador);
    return entrenador;
  }

  private Clase dadoQueTengoUnaClase(Usuario entrenador, String nombre, LocalDateTime inicio) {
    Clase clase = new Clase();
    clase.setNombre(nombre);
    clase.setInicio(inicio);
    clase.setDuracion(45);
    clase.setLugar("Sala 2");
    clase.setCupo(15);
    clase.setCapacidad(CapacidadFisica.CARDIO);
    clase.setEntrenador(entrenador);
    return clase;
  }
}
