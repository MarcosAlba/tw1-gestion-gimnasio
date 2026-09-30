package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Ejercicio;
import com.tallerwebi.dominio.excepcion.EjercicioExistente;
import com.tallerwebi.dominio.excepcion.NombreEjercicioInvalido;
import com.tallerwebi.dominio.interfaces.ServicioEjercicio;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorEjercicio {

  private ServicioEjercicio servEjercicio;
  private static final String DATOS_EJERCICIO = "datosEjercicio";

  @Autowired
  public ControladorEjercicio(ServicioEjercicio servEjercicio) {
    this.servEjercicio = servEjercicio;
  }

  @RequestMapping(path = "/ejercicios/nuevo", method = RequestMethod.GET)
  public ModelAndView irANuevoEjercicio(HttpServletRequest request) {
    if (!"ENTRENADOR".equals(request.getSession().getAttribute("ROL"))) {
      return new ModelAndView("redirect:/login");
    }

    Map<String, Object> modelo = new ModelMap();
    modelo.put(DATOS_EJERCICIO, new DatosEjercicio());
    return new ModelAndView("nuevo-ejercicio", modelo);
  }

  @RequestMapping(path = "/ejercicios/guardar", method = RequestMethod.POST)
  public ModelAndView guardarEjercicio(
    @ModelAttribute(DATOS_EJERCICIO) DatosEjercicio datosEjercicio,
    HttpServletRequest request
  ) {
    if (!"ENTRENADOR".equals(request.getSession().getAttribute("ROL"))) {
      return new ModelAndView("redirect:/login");
    }

    Ejercicio ejercicio = new Ejercicio();
    ejercicio.setNombre(datosEjercicio.getNombre());
    ejercicio.setDescripcion(datosEjercicio.getDescripcion());
    ejercicio.setCapacidad(datosEjercicio.getCapacidad());
    ejercicio.setDificultad(datosEjercicio.getDificultad());

    Map<String, Object> modelo = new ModelMap();
    try {
      servEjercicio.registrar(ejercicio);
    } catch (NombreEjercicioInvalido e) {
      modelo.put(DATOS_EJERCICIO, datosEjercicio);
      modelo.put("error", "El nombre del ejercicio es obligatorio");
      return new ModelAndView("nuevo-ejercicio", modelo);
    } catch (EjercicioExistente e) {
      modelo.put(DATOS_EJERCICIO, datosEjercicio);
      modelo.put("error", "Ya existe un ejercicio con ese nombre");
      return new ModelAndView("nuevo-ejercicio", modelo);
    }
    return new ModelAndView("redirect:/ejercicios");
  }

  @RequestMapping(path = "/ejercicios", method = RequestMethod.GET)
  public ModelAndView listarEjercicios(HttpServletRequest request) {
    if (!"ENTRENADOR".equals(request.getSession().getAttribute("ROL"))) {
      return new ModelAndView("redirect:/login");
    }

    Map<String, Object> modelo = new ModelMap();
    modelo.put("ejercicios", servEjercicio.listarTodos());
    return new ModelAndView("ejercicios", modelo);
  }
}
