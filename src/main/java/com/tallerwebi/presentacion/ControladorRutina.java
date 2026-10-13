package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ItemRutina;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.enums.DiaSemana;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.interfaces.ServicioRutina;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorRutina {

  private static final String ATRIBUTO_ROL = "ROL";
  private static final String ATRIBUTO_ID_USUARIO = "ID_USUARIO";
  private static final String ROL_ENTRENADOR = "ENTRENADOR";
  private static final String ROL_SOCIO = "SOCIO";
  private static final String REDIRECT_LOGIN = "redirect:/login";

  private final ServicioRutina servicioRutina;

  @Autowired
  public ControladorRutina(ServicioRutina servicioRutina) {
    this.servicioRutina = servicioRutina;
  }

  // 1. Ver mi rutina (Socio)
  @GetMapping("/rutina")
  public ModelAndView verRutina(HttpServletRequest request) {
    if (!tieneRol(request, ROL_SOCIO)) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    Long idSocio = (Long) request.getSession().getAttribute(ATRIBUTO_ID_USUARIO);
    Map<DiaSemana, List<ItemRutina>> rutina = servicioRutina.obtenerRutinaPorDias(idSocio);

    ModelAndView modelAndView = new ModelAndView("rutina");
    modelAndView.addObject("rutinaPorDias", rutina);
    return modelAndView;
  }

  // 2. Listado y búsqueda de socios (Entrenador)
  @GetMapping("/socios")
  public ModelAndView listarSocios(
    @RequestParam(value = "buscar", required = false) String buscar,
    HttpServletRequest request
  ) {
    if (!tieneRol(request, ROL_ENTRENADOR)) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    List<Usuario> socios = servicioRutina.buscarSocios(buscar);
    ModelAndView modelAndView = new ModelAndView("socios-lista");
    modelAndView.addObject("socios", socios);
    modelAndView.addObject("filtroBuscar", buscar);
    return modelAndView;
  }

  // 3. Entrenador viendo y editando la rutina de un socio
  @GetMapping("/socios/{idSocio}/rutina")
  public ModelAndView gestionarRutinaSocio(
    @PathVariable("idSocio") Long idSocio,
    HttpServletRequest request
  ) {
    if (!tieneRol(request, ROL_ENTRENADOR)) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    try {
      Usuario socio = servicioRutina.obtenerSocio(idSocio);
      Map<DiaSemana, List<ItemRutina>> rutina = servicioRutina.obtenerRutinaPorDias(idSocio);

      ModelAndView modelAndView = new ModelAndView("asignar-rutina");
      modelAndView.addObject("socio", socio);
      modelAndView.addObject("rutinaPorDias", rutina);
      modelAndView.addObject("nuevoItem", new ItemRutina());
      return modelAndView;
    } catch (UsuarioNoEncontrado exUsuario) {
      return new ModelAndView("redirect:/socios");
    }
  }

  // 4. Guardar ejercicio asignado
  @PostMapping("/socios/{idSocio}/rutina/asignar")
  public ModelAndView asignarEjercicio(
    @PathVariable("idSocio") Long idSocio,
    @ModelAttribute("nuevoItem") ItemRutina item,
    HttpServletRequest request
  ) {
    if (!tieneRol(request, ROL_ENTRENADOR)) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    Long idEntrenador = (Long) request.getSession().getAttribute(ATRIBUTO_ID_USUARIO);
    try {
      servicioRutina.asignarEjercicio(idSocio, idEntrenador, item);
    } catch (UsuarioNoEncontrado exUsuario) {
      return new ModelAndView("redirect:/socios");
    }
    return new ModelAndView("redirect:/socios/" + idSocio + "/rutina");
  }

  // 5. Eliminar ejercicio de la rutina
  @PostMapping("/socios/{idSocio}/rutina/eliminar")
  public ModelAndView eliminarEjercicio(
    @PathVariable("idSocio") Long idSocio,
    @RequestParam("idItem") Long idItem,
    HttpServletRequest request
  ) {
    if (!tieneRol(request, ROL_ENTRENADOR)) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    servicioRutina.eliminarEjercicio(idItem);
    return new ModelAndView("redirect:/socios/" + idSocio + "/rutina");
  }

  private boolean tieneRol(HttpServletRequest request, String rolEsperado) {
    HttpSession sesion = request.getSession();
    String rolActual = (String) sesion.getAttribute(ATRIBUTO_ROL);
    return rolEsperado.equals(rolActual);
  }

  @PostMapping("/rutina/item/{idItem}/checkin")
  public ModelAndView registrarAvance(
    @PathVariable("idItem") Long idItem,
    @RequestParam(value = "pesoReal", required = false) Double pesoReal,
    HttpServletRequest request
  ) {
    if (!tieneRol(request, ROL_SOCIO)) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    Long idSocio = (Long) request.getSession().getAttribute(ATRIBUTO_ID_USUARIO);
    servicioRutina.registrarAvance(idItem, idSocio, pesoReal);
    return new ModelAndView("redirect:/rutina");
  }
}
