package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.tallerwebi.dominio.Ejercicio;
import com.tallerwebi.dominio.Rutina;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.enums.Deporte;
import com.tallerwebi.dominio.enums.NivelDificultad;
import com.tallerwebi.dominio.interfaces.RepositorioRutina;
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
public class RepositorioRutinaTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioRutina repoRutina;

  @BeforeEach
  public void init() {
    repoRutina = new RepositorioRutinaImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnaRutina() {
    Usuario socio = dadoQueExisteUnSocio("socio@test.com");
    Ejercicio zigzag = dadoQueExisteUnEjercicio("Zigzag entre conos");
    Ejercicio escalera = dadoQueExisteUnEjercicio("Escalera de agilidad");
    LocalDateTime fecha = LocalDateTime.now();
    Rutina rutina = dadoQueTengoUnaRutina(socio, fecha, List.of(zigzag, escalera));

    repoRutina.guardar(rutina);
    Rutina obtenida = repoRutina.buscarUltimaDeSocio(socio.getId());

    assertThat(obtenida.getId(), notNullValue());
    assertThat(obtenida.getSocio(), equalTo(socio));
    assertThat(obtenida.getDeporte(), equalTo(Deporte.TENIS));
    assertThat(obtenida.getFechaCreacion(), equalTo(fecha));
    assertThat(obtenida.getEjercicios(), hasSize(2));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaDevolverLaRutinaMasReciente() {
    Usuario socio = dadoQueExisteUnSocio("socio@test.com");
    Ejercicio zigzag = dadoQueExisteUnEjercicio("Zigzag entre conos");
    Rutina rutinaDeAyer = dadoQueTengoUnaRutina(
      socio,
      LocalDateTime.now().minusDays(1),
      List.of(zigzag)
    );
    Rutina rutinaDeHoy = dadoQueTengoUnaRutina(socio, LocalDateTime.now(), List.of(zigzag));

    repoRutina.guardar(rutinaDeAyer);
    repoRutina.guardar(rutinaDeHoy);

    Rutina obtenida = repoRutina.buscarUltimaDeSocio(socio.getId());

    assertThat(obtenida, equalTo(rutinaDeHoy));
  }

  @Test
  @Transactional
  public void noDeberiaEncontrarRutinaSiElSocioNoTiene() {
    Usuario socio = dadoQueExisteUnSocio("socio@test.com");
    Rutina obtenida = repoRutina.buscarUltimaDeSocio(socio.getId());

    assertThat(obtenida, is(nullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaDevolverLaRutinaDeOtroSocio() {
    Usuario socioConRutina = dadoQueExisteUnSocio("con.rutina@test.com");
    Usuario socioSinRutina = dadoQueExisteUnSocio("sin.rutina@test.com");
    Ejercicio zigzag = dadoQueExisteUnEjercicio("Zigzag entre conos");
    repoRutina.guardar(dadoQueTengoUnaRutina(socioConRutina, LocalDateTime.now(), List.of(zigzag)));

    Rutina obtenida = repoRutina.buscarUltimaDeSocio(socioSinRutina.getId());

    assertThat(obtenida, is(nullValue()));
  }

  private Usuario dadoQueExisteUnSocio(String email) {
    Usuario socio = new Usuario();
    socio.setEmail(email);
    socio.setPassword("123");
    socio.setRol("SOCIO");
    socio.setDeporte(Deporte.TENIS);
    sessionFactory.getCurrentSession().persist(socio);
    return socio;
  }

  private Ejercicio dadoQueExisteUnEjercicio(String nombre) {
    Ejercicio ejercicio = new Ejercicio();
    ejercicio.setNombre(nombre);
    ejercicio.setCapacidad(CapacidadFisica.AGILIDAD);
    ejercicio.setDificultad(NivelDificultad.INTERMEDIO);
    sessionFactory.getCurrentSession().persist(ejercicio);
    return ejercicio;
  }

  private Rutina dadoQueTengoUnaRutina(
    Usuario socio,
    LocalDateTime fecha,
    List<Ejercicio> ejercicios
  ) {
    Rutina rutina = new Rutina();
    rutina.setSocio(socio);
    rutina.setDeporte(Deporte.TENIS);
    rutina.setFechaCreacion(fecha);
    rutina.setEjercicios(ejercicios);
    return rutina;
  }
}
