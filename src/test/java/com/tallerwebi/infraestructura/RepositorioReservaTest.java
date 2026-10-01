package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.Clase;
import com.tallerwebi.dominio.Reserva;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.enums.EstadoReserva;
import com.tallerwebi.dominio.interfaces.RepositorioReserva;
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
public class RepositorioReservaTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioReserva repoReserva;

  @BeforeEach
  public void init() {
    repoReserva = new RepositorioReservaImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnaReservaYBuscarlaPorId() {
    Usuario socio = dadoQueExisteUnSocio("socio@test.com");
    Clase clase = dadoQueExisteUnaClase("Spinning");
    LocalDateTime fecha = LocalDateTime.now();
    Reserva reserva = dadoQueTengoUnaReserva(socio, clase, EstadoReserva.CONFIRMADA, fecha);

    repoReserva.guardar(reserva);
    Reserva obtenida = repoReserva.buscarPorId(reserva.getId());

    assertThat(obtenida.getId(), notNullValue());
    assertThat(obtenida.getSocio(), equalTo(socio));
    assertThat(obtenida.getClase(), equalTo(clase));
    assertThat(obtenida.getEstado(), equalTo(EstadoReserva.CONFIRMADA));
    assertThat(obtenida.getFechaReserva(), equalTo(fecha));
  }

  @Test
  @Transactional
  public void noDeberiaEncontrarUnaReservaInexistentePorId() {
    Reserva obtenida = repoReserva.buscarPorId(999L);

    assertThat(obtenida, is(nullValue()));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaModificarElEstadoDeUnaReserva() {
    Usuario socio = dadoQueExisteUnSocio("socio@test.com");
    Clase clase = dadoQueExisteUnaClase("Spinning");
    Reserva reserva = dadoQueTengoUnaReserva(
      socio,
      clase,
      EstadoReserva.CONFIRMADA,
      LocalDateTime.now()
    );
    repoReserva.guardar(reserva);

    reserva.setEstado(EstadoReserva.CANCELADA);
    repoReserva.modificar(reserva);
    Reserva obtenida = repoReserva.buscarPorId(reserva.getId());

    assertThat(obtenida.getEstado(), equalTo(EstadoReserva.CANCELADA));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarSoloLasReservasDelSocioDeLaMasRecienteALaMasVieja() {
    Usuario socio = dadoQueExisteUnSocio("socio@test.com");
    Usuario otroSocio = dadoQueExisteUnSocio("otro@test.com");
    Clase clase = dadoQueExisteUnaClase("Spinning");
    Reserva vieja = dadoQueTengoUnaReserva(
      socio,
      clase,
      EstadoReserva.CANCELADA,
      LocalDateTime.now().minusDays(2)
    );
    Reserva nueva = dadoQueTengoUnaReserva(
      socio,
      clase,
      EstadoReserva.CONFIRMADA,
      LocalDateTime.now()
    );
    repoReserva.guardar(vieja);
    repoReserva.guardar(nueva);
    repoReserva.guardar(
      dadoQueTengoUnaReserva(otroSocio, clase, EstadoReserva.CONFIRMADA, LocalDateTime.now())
    );

    List<Reserva> reservas = repoReserva.buscarPorSocio(socio.getId());

    assertThat(reservas, hasSize(2));
    assertThat(reservas.get(0), equalTo(nueva));
    assertThat(reservas.get(1), equalTo(vieja));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaContarSoloLasReservasConfirmadasDeLaClase() {
    Usuario socio = dadoQueExisteUnSocio("socio@test.com");
    Usuario otroSocio = dadoQueExisteUnSocio("otro@test.com");
    Clase clase = dadoQueExisteUnaClase("Spinning");
    Clase otraClase = dadoQueExisteUnaClase("Funcional");
    repoReserva.guardar(
      dadoQueTengoUnaReserva(socio, clase, EstadoReserva.CONFIRMADA, LocalDateTime.now())
    );
    repoReserva.guardar(
      dadoQueTengoUnaReserva(otroSocio, clase, EstadoReserva.CONFIRMADA, LocalDateTime.now())
    );
    repoReserva.guardar(
      dadoQueTengoUnaReserva(socio, clase, EstadoReserva.CANCELADA, LocalDateTime.now())
    );
    repoReserva.guardar(
      dadoQueTengoUnaReserva(socio, otraClase, EstadoReserva.CONFIRMADA, LocalDateTime.now())
    );

    int confirmadas = repoReserva.contarConfirmadas(clase.getId());

    assertThat(confirmadas, equalTo(2));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaContarCeroSiLaClaseNoTieneReservas() {
    Clase clase = dadoQueExisteUnaClase("Spinning");

    int confirmadas = repoReserva.contarConfirmadas(clase.getId());

    assertThat(confirmadas, equalTo(0));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaExistirUnaReservaConfirmadaDelSocioParaLaClase() {
    Usuario socio = dadoQueExisteUnSocio("socio@test.com");
    Clase clase = dadoQueExisteUnaClase("Spinning");
    repoReserva.guardar(
      dadoQueTengoUnaReserva(socio, clase, EstadoReserva.CONFIRMADA, LocalDateTime.now())
    );

    boolean existe = repoReserva.existeConfirmada(socio.getId(), clase.getId());

    assertThat(existe, is(true));
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaExistirUnaReservaConfirmadaSiEstaCanceladaOEsDeOtroSocio() {
    Usuario socio = dadoQueExisteUnSocio("socio@test.com");
    Usuario otroSocio = dadoQueExisteUnSocio("otro@test.com");
    Clase clase = dadoQueExisteUnaClase("Spinning");
    repoReserva.guardar(
      dadoQueTengoUnaReserva(socio, clase, EstadoReserva.CANCELADA, LocalDateTime.now())
    );

    assertThat(repoReserva.existeConfirmada(socio.getId(), clase.getId()), is(false));
    assertThat(repoReserva.existeConfirmada(otroSocio.getId(), clase.getId()), is(false));
  }

  private Usuario dadoQueExisteUnSocio(String email) {
    Usuario socio = new Usuario();
    socio.setEmail(email);
    socio.setPassword("123");
    socio.setRol("SOCIO");
    sessionFactory.getCurrentSession().persist(socio);
    return socio;
  }

  private Clase dadoQueExisteUnaClase(String nombre) {
    Clase clase = new Clase();
    clase.setNombre(nombre);
    clase.setInicio(LocalDateTime.now().plusDays(1));
    clase.setCupo(10);
    sessionFactory.getCurrentSession().persist(clase);
    return clase;
  }

  private Reserva dadoQueTengoUnaReserva(
    Usuario socio,
    Clase clase,
    EstadoReserva estado,
    LocalDateTime fecha
  ) {
    Reserva reserva = new Reserva();
    reserva.setSocio(socio);
    reserva.setClase(clase);
    reserva.setEstado(estado);
    reserva.setFechaReserva(fecha);
    return reserva;
  }
}
