package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.dominio.Clase;
import com.tallerwebi.dominio.Ejercicio;
import com.tallerwebi.dominio.Membresia;
import com.tallerwebi.dominio.Reserva;
import com.tallerwebi.dominio.Rutina;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.enums.Deporte;
import com.tallerwebi.dominio.enums.EstadoReserva;
import com.tallerwebi.dominio.enums.NivelDificultad;
import com.tallerwebi.dominio.enums.TipoMembresia;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Renderiza cada pantalla con datos reales para comprobar que las plantillas
 * (y los fragmentos compartidos) se procesan sin errores.
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class VistasTest {

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private SessionFactory sessionFactory;

  private MockMvc mockMvc;
  private Usuario socio;
  private Usuario entrenador;

  @BeforeEach
  public void init() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  public void deberiaMostrarLaMarcaUnlamEnElLogin() throws Exception {
    String html = pagina(get("/login"));

    assertThat(html, containsString("class=\"navbar-brand\""));
    assertThat(html, containsString(">UNLAM</a>"));
    assertThat(html, containsString("id=\"btn-login\""));
    assertThat(html, containsString("id=\"btn-register\""));
  }

  @Test
  public void deberiaMostrarElRegistro() throws Exception {
    String html = pagina(get("/nuevo-usuario"));

    assertThat(html, containsString("id=\"btn-registrarme\""));
    assertThat(html, containsString("id=\"rol\""));
    assertThat(html, containsString("Entrenador"));
    assertThat(html, containsString("Fútbol"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaMostrarLosAccesosDelSocioEnElInicio() throws Exception {
    dadoQueExistenDatosDeEjemplo();

    String html = pagina(comoSocio(get("/home")));

    assertThat(html, containsString("Tu entrenamiento"));
    assertThat(html, containsString("Mis reservas"));
    assertThat(html, containsString("Mi rutina"));
    assertThat(html, not(containsString("Cargá ejercicios")));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaMostrarLosAccesosDelEntrenadorEnElInicio() throws Exception {
    dadoQueExistenDatosDeEjemplo();

    String html = pagina(comoEntrenador(get("/home")));

    assertThat(html, containsString("Tus clases"));
    assertThat(html, containsString("Cargá ejercicios"));
    assertThat(html, not(containsString("Mi rutina")));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaMostrarLasClasesConSuCapacidadYElBotonDeReservaParaElSocio() throws Exception {
    dadoQueExistenDatosDeEjemplo();

    String html = pagina(comoSocio(get("/clases")));

    assertThat(html, containsString("Spinning"));
    assertThat(html, containsString("disco-cardio"));
    assertThat(html, containsString("Reservar"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaOfrecerCrearClasesAlEntrenador() throws Exception {
    dadoQueExistenDatosDeEjemplo();

    String html = pagina(comoEntrenador(get("/clases")));

    assertThat(html, containsString("Nueva clase"));
    assertThat(html, not(containsString("Reservar")));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaMostrarElFormularioDeNuevaClase() throws Exception {
    String html = pagina(comoEntrenador(get("/clases/nueva")));

    assertThat(html, containsString("Coordinación"));
    assertThat(html, containsString("type=\"datetime-local\""));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaMostrarLasReservasConSuEstado() throws Exception {
    dadoQueExistenDatosDeEjemplo();

    String html = pagina(comoSocio(get("/reservas")));

    assertThat(html, containsString("Confirmada"));
    assertThat(html, containsString("Cancelada"));
    assertThat(html, containsString("estado-cancelada"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaMostrarLaMembresiaVigenteYElHistorial() throws Exception {
    dadoQueExistenDatosDeEjemplo();

    String html = pagina(comoSocio(get("/membresias")));

    assertThat(html, containsString("Vigente. Vence el"));
    assertThat(html, containsString("Trimestral"));
    assertThat(html, containsString("Anual (12 meses)"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaMostrarLaRutinaConLaDificultadComoSegmentos() throws Exception {
    dadoQueExistenDatosDeEjemplo();

    String html = pagina(comoSocio(get("/rutina")));

    assertThat(html, containsString("Tenis"));
    assertThat(html, containsString("disco-fuerza"));
    assertThat(html, containsString("disco-coordinacion"));
    assertThat(html, containsString("nivel-barras"));
    assertThat(html, containsString("Avanzado"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaMostrarElListadoDeEjerciciosAlEntrenador() throws Exception {
    dadoQueExistenDatosDeEjemplo();

    String html = pagina(comoEntrenador(get("/ejercicios")));

    assertThat(html, containsString("Sentadilla"));
    assertThat(html, containsString("disco-fuerza"));
    assertThat(html, containsString("disco-agilidad"));
    assertThat(html, containsString("Principiante"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaMostrarElFormularioDeNuevoEjercicio() throws Exception {
    String html = pagina(comoEntrenador(get("/ejercicios/nuevo")));

    assertThat(html, containsString("Coordinación"));
    assertThat(html, containsString("Intermedio"));
  }

  @Test
  public void noDeberiaMostrarLasClasesSinUnaSesionIniciada() throws Exception {
    mockMvc.perform(get("/clases")).andExpect(redirectedUrl("/login"));
  }

  private String pagina(MockHttpServletRequestBuilder pedido) throws Exception {
    return mockMvc
      .perform(pedido)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString(StandardCharsets.UTF_8);
  }

  private MockHttpServletRequestBuilder comoSocio(MockHttpServletRequestBuilder pedido) {
    Long id = socio == null ? 1L : socio.getId();
    return pedido.sessionAttr("ROL", "SOCIO").sessionAttr("ID_USUARIO", id);
  }

  private MockHttpServletRequestBuilder comoEntrenador(MockHttpServletRequestBuilder pedido) {
    Long id = entrenador == null ? 2L : entrenador.getId();
    return pedido.sessionAttr("ROL", "ENTRENADOR").sessionAttr("ID_USUARIO", id);
  }

  private void dadoQueExistenDatosDeEjemplo() {
    Session sesion = sessionFactory.getCurrentSession();

    entrenador = usuario("entrenador@test.com", "ENTRENADOR", null);
    socio = usuario("socio@test.com", "SOCIO", Deporte.TENIS);
    sesion.persist(entrenador);
    sesion.persist(socio);

    sesion.persist(new Membresia(socio, TipoMembresia.TRIMESTRAL, LocalDate.now()));
    sesion.persist(new Membresia(socio, TipoMembresia.ANUAL, LocalDate.now().minusYears(2)));

    Clase spinning = clase("Spinning", CapacidadFisica.CARDIO, 1);
    Clase funcional = clase("Funcional", CapacidadFisica.FUERZA, 2);
    sesion.persist(spinning);
    sesion.persist(funcional);
    sesion.persist(reserva(spinning, EstadoReserva.CONFIRMADA));
    sesion.persist(reserva(funcional, EstadoReserva.CANCELADA));

    List<Ejercicio> ejercicios = new ArrayList<>();
    ejercicios.add(ejercicio("Sentadilla", CapacidadFisica.FUERZA, NivelDificultad.AVANZADO));
    ejercicios.add(ejercicio("Zigzag", CapacidadFisica.AGILIDAD, NivelDificultad.PRINCIPIANTE));
    ejercicios.add(ejercicio("Soga", CapacidadFisica.CARDIO, NivelDificultad.INTERMEDIO));
    ejercicios.add(ejercicio("Bosu", CapacidadFisica.COORDINACION, NivelDificultad.INTERMEDIO));
    ejercicios.forEach(sesion::persist);

    Rutina rutina = new Rutina();
    rutina.setSocio(socio);
    rutina.setDeporte(Deporte.TENIS);
    rutina.setFechaCreacion(LocalDateTime.now());
    rutina.setEjercicios(ejercicios);
    sesion.persist(rutina);
  }

  private Usuario usuario(String email, String rol, Deporte deporte) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword("123");
    usuario.setRol(rol);
    usuario.setDeporte(deporte);
    return usuario;
  }

  private Clase clase(String nombre, CapacidadFisica capacidad, int dias) {
    Clase clase = new Clase();
    clase.setNombre(nombre);
    clase.setInicio(LocalDateTime.now().plusDays(dias));
    clase.setDuracion(45);
    clase.setLugar("Sala 1");
    clase.setCupo(10);
    clase.setCapacidad(capacidad);
    clase.setEntrenador(entrenador);
    return clase;
  }

  private Reserva reserva(Clase clase, EstadoReserva estado) {
    Reserva reserva = new Reserva();
    reserva.setSocio(socio);
    reserva.setClase(clase);
    reserva.setEstado(estado);
    reserva.setFechaReserva(LocalDateTime.now());
    return reserva;
  }

  private Ejercicio ejercicio(String nombre, CapacidadFisica capacidad, NivelDificultad nivel) {
    Ejercicio ejercicio = new Ejercicio();
    ejercicio.setNombre(nombre);
    ejercicio.setDescripcion("Descripcion de " + nombre);
    ejercicio.setCapacidad(capacidad);
    ejercicio.setDificultad(nivel);
    return ejercicio;
  }
}
