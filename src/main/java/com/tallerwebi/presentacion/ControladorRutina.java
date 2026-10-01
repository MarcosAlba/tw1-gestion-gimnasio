package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.excepcion.SocioSinDeporte;
import com.tallerwebi.dominio.interfaces.ServicioRutina;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorRutina {

  private ServicioRutina servicioRutina;

  @Autowired
  public ControladorRutina(ServicioRutina servicioRutina) {
    this.servicioRutina = servicioRutina;
  }

  @RequestMapping(path = "/rutina", method = RequestMethod.GET)
  public ModelAndView verRutina(HttpServletRequest request) {
    if (!"SOCIO".equals(request.getSession().getAttribute("ROL"))) {
      return new ModelAndView("redirect:/login");
    }
    Long socioId = (Long) request.getSession().getAttribute("ID_USUARIO");

    Map<String, Object> modelo = new ModelMap();
    modelo.put("rutina", servicioRutina.obtenerUltima(socioId));
    return new ModelAndView("rutina", modelo);
  }

  @RequestMapping(path = "/rutina/generar", method = RequestMethod.POST)
  public ModelAndView generarRutina(HttpServletRequest request) {
    if (!"SOCIO".equals(request.getSession().getAttribute("ROL"))) {
      return new ModelAndView("redirect:/login");
    }
    Long socioId = (Long) request.getSession().getAttribute("ID_USUARIO");

    try {
      servicioRutina.generar(socioId);
    } catch (SocioSinDeporte e) {
      Map<String, Object> modelo = new ModelMap();
      modelo.put("error", "No tenés un deporte asociado para generar la rutina");
      return new ModelAndView("rutina", modelo);
    }
    return new ModelAndView("redirect:/rutina");
  }
}
