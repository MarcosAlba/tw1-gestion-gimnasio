package com.tallerwebi.integracion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.enums.Deporte;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.List;
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

/** El formulario de registro de punta a punta: formulario, controlador, servicio y base. */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })
public class RegistroConRolTest {

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private SessionFactory sessionFactory;

  private MockMvc mockMvc;

  @BeforeEach
  public void init() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaRegistrarUnSocioConSuDeporteYVolverAlLogin() throws Exception {
    mockMvc
      .perform(formulario("socio@test.com", "SOCIO").param("deporte", "TENIS"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/login"));

    Usuario guardado = buscar("socio@test.com");
    assertThat(guardado.getRol(), equalTo("SOCIO"));
    assertThat(guardado.getDeporte(), equalTo(Deporte.TENIS));
    assertThat(guardado.getNombre(), equalTo("Nombre"));
    assertThat(guardado.getApellido(), equalTo("Apellido"));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaRegistrarUnEntrenadorSinDeporte() throws Exception {
    mockMvc
      .perform(formulario("entrenador@test.com", "ENTRENADOR").param("deporte", "TENIS"))
      .andExpect(redirectedUrl("/login"));

    Usuario guardado = buscar("entrenador@test.com");
    assertThat(guardado.getRol(), equalTo("ENTRENADOR"));
    assertThat(guardado.getDeporte(), nullValue());
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaPermitirRegistrarseComoAdministrador() throws Exception {
    String html = pagina(formulario("admin@test.com", "ADMIN"));

    assertThat(html, containsString("El rol elegido no es válido"));
    assertThat(usuariosConEmail("admin@test.com"), hasSize(0));
  }

  @Test
  @Transactional
  @Rollback
  public void noDeberiaRegistrarDosVecesElMismoEmailAunqueCambieLaClave() throws Exception {
    mockMvc.perform(formulario("repetido@test.com", "SOCIO")).andExpect(redirectedUrl("/login"));

    String html = pagina(formulario("repetido@test.com", "SOCIO").param("password", "otra"));

    assertThat(html, containsString("El usuario ya existe"));
    assertThat(usuariosConEmail("repetido@test.com"), hasSize(1));
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaConservarLoEscritoCuandoElRegistroFalla() throws Exception {
    String html = pagina(formulario("admin@test.com", "ADMIN"));

    assertThat(html, containsString("value=\"admin@test.com\""));
    assertThat(html, containsString("value=\"Nombre\""));
  }

  private MockHttpServletRequestBuilder formulario(String email, String rol) {
    return post("/registrarme")
      .param("nombre", "Nombre")
      .param("apellido", "Apellido")
      .param("email", email)
      .param("password", "123")
      .param("rol", rol);
  }

  private String pagina(MockHttpServletRequestBuilder pedido) throws Exception {
    return mockMvc
      .perform(pedido)
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString(StandardCharsets.UTF_8);
  }

  private List<Usuario> usuariosConEmail(String email) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Usuario where email = :email", Usuario.class)
      .setParameter("email", email)
      .getResultList();
  }

  private Usuario buscar(String email) {
    return usuariosConEmail(email).get(0);
  }
}
