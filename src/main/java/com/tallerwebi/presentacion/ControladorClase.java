package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Clase;
import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.excepcion.EntrenadorInvalido;
import com.tallerwebi.dominio.interfaces.ServicioClase;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorClase {

  private static final String DATOS_CLASE = "datosClase";
  private static final String VISTA_NUEVA_CLASE = "nueva-clase";
  private static final String REDIRECT_LOGIN = "redirect:/login";

  private ServicioClase servicioClase;

  @Autowired
  public ControladorClase(ServicioClase servicioClase) {
    this.servicioClase = servicioClase;
  }

  @RequestMapping(path = "/clases/nueva", method = RequestMethod.GET)
  public ModelAndView irANuevaClase(HttpServletRequest request) {
    if (!esEntrenador(request)) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    Map<String, Object> modelo = new ModelMap();
    modelo.put(DATOS_CLASE, new DatosClase());
    return new ModelAndView(VISTA_NUEVA_CLASE, modelo);
  }

  @RequestMapping(path = "/clases/guardar", method = RequestMethod.POST)
  public ModelAndView guardarClase(
    @ModelAttribute(DATOS_CLASE) DatosClase datosClase,
    HttpServletRequest request
  ) {
    if (!esEntrenador(request)) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    Long entrenadorId = (Long) request.getSession().getAttribute("ID_USUARIO");

    Clase clase = new Clase();
    clase.setNombre(datosClase.getNombre());
    clase.setInicio(datosClase.getInicio());
    clase.setDuracion(datosClase.getDuracion());
    clase.setLugar(datosClase.getLugar());
    clase.setCupo(datosClase.getCupo());
    clase.setCapacidad(datosClase.getCapacidad());

    try {
      servicioClase.crear(clase, entrenadorId);
    } catch (EntrenadorInvalido e) {
      Map<String, Object> modelo = new ModelMap();
      modelo.put(DATOS_CLASE, datosClase);
      modelo.put("error", "El usuario no es un entrenador valido");
      return new ModelAndView(VISTA_NUEVA_CLASE, modelo);
    }
    return new ModelAndView("redirect:/clases");
  }

  @RequestMapping(path = "/clases", method = RequestMethod.GET)
  public ModelAndView listarClases(HttpServletRequest request) {
    if (request.getSession().getAttribute("ROL") == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    Map<String, Object> modelo = new ModelMap();
    modelo.put("clases", servicioClase.listarProximas());
    return new ModelAndView("clases", modelo);
  }

  // Página pública: no pide sesión. Una capacidad inválida equivale a ver todas.
  @RequestMapping(path = "/horarios", method = RequestMethod.GET)
  public ModelAndView verHorarios(
    @RequestParam(name = "capacidad", required = false) String capacidad
  ) {
    CapacidadFisica capacidadSeleccionada = interpretarCapacidad(capacidad);

    Map<String, Object> modelo = new ModelMap();
    modelo.put("clasesPorDia", servicioClase.listarSemana(capacidadSeleccionada));
    modelo.put("capacidadSeleccionada", capacidadSeleccionada);
    return new ModelAndView("horarios", modelo);
  }

  private CapacidadFisica interpretarCapacidad(String capacidad) {
    if (capacidad == null) {
      return null;
    }
    try {
      return CapacidadFisica.valueOf(capacidad.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private boolean esEntrenador(HttpServletRequest request) {
    return "ENTRENADOR".equals(request.getSession().getAttribute("ROL"));
  }
}
