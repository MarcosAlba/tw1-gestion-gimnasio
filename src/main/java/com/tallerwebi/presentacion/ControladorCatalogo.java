package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.EjercicioApi;
import com.tallerwebi.dominio.interfaces.ServicioCatalogoApi;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorCatalogo {

  private ServicioCatalogoApi servicioCatalogoApi;

  @Autowired
  public ControladorCatalogo(ServicioCatalogoApi servicioCatalogoApi) {
    this.servicioCatalogoApi = servicioCatalogoApi;
  }

  @RequestMapping(path = "/catalogo", method = RequestMethod.GET)
  public ModelAndView verCatalogo(
    @RequestParam(value = "buscar", required = false) String buscar,
    @RequestParam(value = "equipamiento", required = false) String equipamiento,
    @RequestParam(value = "grupo", required = false) String grupo,
    @RequestParam(value = "genero", required = false, defaultValue = "male") String genero,
    HttpServletRequest request
  ) {
    if (request.getSession().getAttribute("ROL") == null) {
      return new ModelAndView("redirect:/login");
    }

    List<EjercicioApi> ejercicios = servicioCatalogoApi.obtenerEjercicios(
      buscar,
      equipamiento,
      grupo
    );
    Set<String> equipamientos = servicioCatalogoApi.obtenerEquipamientos();
    Set<String> grupos = servicioCatalogoApi.obtenerGrupos();

    Map<String, Object> modelo = new ModelMap();
    modelo.put("ejercicios", ejercicios != null ? ejercicios : new ArrayList<>());
    modelo.put("equipamientos", equipamientos != null ? equipamientos : new HashSet<>());
    modelo.put("grupos", grupos != null ? grupos : new HashSet<>());
    modelo.put("filtroBuscar", buscar != null ? buscar : "");
    modelo.put("filtroEquipamiento", equipamiento != null ? equipamiento : "");
    modelo.put("filtroGrupo", grupo != null ? grupo : "");
    modelo.put("filtroGenero", "female".equalsIgnoreCase(genero) ? "female" : "male");

    return new ModelAndView("catalogo", modelo);
  }

  @RequestMapping(path = "/catalogo/{slug}", method = RequestMethod.GET)
  public ModelAndView verDetalle(
    @org.springframework.web.bind.annotation.PathVariable("slug") String slug,
    HttpServletRequest request
  ) {
    if (request.getSession().getAttribute("ROL") == null) {
      return new ModelAndView("redirect:/login");
    }

    EjercicioApi ejercicio = servicioCatalogoApi.buscarPorSlug(slug);
    if (ejercicio == null) {
      return new ModelAndView("redirect:/catalogo");
    }

    Map<String, Object> modelo = new ModelMap();
    modelo.put("ejercicio", ejercicio);
    return new ModelAndView("detalle-ejercicio-api", modelo);
  }
}
