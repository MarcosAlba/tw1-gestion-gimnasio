package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.tallerwebi.dominio.Membresia;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.enums.TipoMembresia;
import com.tallerwebi.dominio.interfaces.RepositorioMembresia;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
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
public class RepositorioMembresiaTest {

  @Autowired
  private SessionFactory sessionFactory;

  private RepositorioMembresia repoMembresia;

  @BeforeEach
  public void init() {
    repoMembresia = new RepositorioMembresiaImpl(sessionFactory);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnaMembresia() {
    Usuario socio = dadoQueExisteUnSocio("socio@test.com");
    LocalDate hoy = LocalDate.now();
    Membresia membresia = dadoQueTengoUnaMembresia(socio, TipoMembresia.TRIMESTRAL, hoy);

    repoMembresia.guardar(membresia);
    List<Membresia> membresias = repoMembresia.buscarPorSocio(socio.getId());

    assertThat(membresias, hasSize(1));
    Membresia obtenida = membresias.get(0);
    assertThat(obtenida.getId(), notNullValue());
    assertThat(obtenida.getSocio(), equalTo(socio));
    assertThat(obtenida.getTipo(), equalTo(TipoMembresia.TRIMESTRAL));
    assertThat(obtenida.getFechaInicio(), equalTo(hoy));
    assertThat(obtenida.getFechaVencimiento(), equalTo(hoy.plusMonths(3)));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarSoloLasMembresiasDelSocio() {
    Usuario socio = dadoQueExisteUnSocio("socio@test.com");
    Usuario otroSocio = dadoQueExisteUnSocio("otro@test.com");
    repoMembresia.guardar(
      dadoQueTengoUnaMembresia(socio, TipoMembresia.MENSUAL, LocalDate.now().minusMonths(2))
    );
    repoMembresia.guardar(dadoQueTengoUnaMembresia(socio, TipoMembresia.ANUAL, LocalDate.now()));
    repoMembresia.guardar(
      dadoQueTengoUnaMembresia(otroSocio, TipoMembresia.MENSUAL, LocalDate.now())
    );

    List<Membresia> membresias = repoMembresia.buscarPorSocio(socio.getId());

    assertThat(membresias, hasSize(2));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaBuscarLasMembresiasDeLaMasRecienteALaMasVieja() {
    Usuario socio = dadoQueExisteUnSocio("socio@test.com");
    Membresia vieja = dadoQueTengoUnaMembresia(
      socio,
      TipoMembresia.MENSUAL,
      LocalDate.now().minusMonths(2)
    );
    Membresia nueva = dadoQueTengoUnaMembresia(socio, TipoMembresia.ANUAL, LocalDate.now());
    repoMembresia.guardar(vieja);
    repoMembresia.guardar(nueva);

    List<Membresia> membresias = repoMembresia.buscarPorSocio(socio.getId());

    assertThat(membresias.get(0), equalTo(nueva));
    assertThat(membresias.get(1), equalTo(vieja));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaEncontrarLaMembresiaVigente() {
    Usuario socio = dadoQueExisteUnSocio("socio@test.com");
    Membresia vencida = dadoQueTengoUnaMembresia(
      socio,
      TipoMembresia.MENSUAL,
      LocalDate.now().minusMonths(2)
    );
    Membresia vigente = dadoQueTengoUnaMembresia(
      socio,
      TipoMembresia.TRIMESTRAL,
      LocalDate.now().minusMonths(1)
    );
    repoMembresia.guardar(vencida);
    repoMembresia.guardar(vigente);

    Membresia obtenida = repoMembresia.buscarVigente(socio.getId(), LocalDate.now());

    assertThat(obtenida, equalTo(vigente));
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaEncontrarMembresiaVigenteSiTodasEstanVencidas() {
    Usuario socio = dadoQueExisteUnSocio("socio@test.com");
    repoMembresia.guardar(
      dadoQueTengoUnaMembresia(socio, TipoMembresia.MENSUAL, LocalDate.now().minusMonths(2))
    );

    Membresia obtenida = repoMembresia.buscarVigente(socio.getId(), LocalDate.now());

    assertThat(obtenida, is(nullValue()));
  }

  @Test
  @Transactional
  public void noDeberiaEncontrarMembresiaVigenteSiElSocioNoTiene() {
    Usuario socio = dadoQueExisteUnSocio("socio@test.com");

    Membresia obtenida = repoMembresia.buscarVigente(socio.getId(), LocalDate.now());

    assertThat(obtenida, is(nullValue()));
  }

  private Usuario dadoQueExisteUnSocio(String email) {
    Usuario socio = new Usuario();
    socio.setEmail(email);
    socio.setPassword("123");
    socio.setRol("SOCIO");
    sessionFactory.getCurrentSession().persist(socio);
    return socio;
  }

  private Membresia dadoQueTengoUnaMembresia(
    Usuario socio,
    TipoMembresia tipo,
    LocalDate fechaInicio
  ) {
    Membresia membresia = new Membresia();
    membresia.setSocio(socio);
    membresia.setTipo(tipo);
    membresia.setFechaInicio(fechaInicio);
    membresia.setFechaVencimiento(tipo.calcularVencimiento(fechaInicio));
    return membresia;
  }
}
