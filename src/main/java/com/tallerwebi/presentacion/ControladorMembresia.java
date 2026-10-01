package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.enums.TipoMembresia;
import com.tallerwebi.dominio.interfaces.ServicioMembresia;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorMembresia {

  private ServicioMembresia servicioMembresia;

  @Autowired
  public ControladorMembresia(ServicioMembresia servicioMembresia) {
    this.servicioMembresia = servicioMembresia;
  }

  @RequestMapping(path = "/membresias", method = RequestMethod.GET)
  public ModelAndView verMembresias(HttpServletRequest request) {
    if (!"SOCIO".equals(request.getSession().getAttribute("ROL"))) {
      return new ModelAndView("redirect:/login");
    }
    Long socioId = (Long) request.getSession().getAttribute("ID_USUARIO");

    Map<String, Object> modelo = new ModelMap();
    modelo.put("historial", servicioMembresia.historial(socioId));
    modelo.put("vigente", servicioMembresia.obtenerVigente(socioId));
    return new ModelAndView("membresias", modelo);
  }

  @RequestMapping(path = "/membresias/guardar", method = RequestMethod.POST)
  public ModelAndView contratarMembresia(
    @RequestParam("tipo") TipoMembresia tipo,
    HttpServletRequest request
  ) {
    if (!"SOCIO".equals(request.getSession().getAttribute("ROL"))) {
      return new ModelAndView("redirect:/login");
    }
    Long socioId = (Long) request.getSession().getAttribute("ID_USUARIO");

    servicioMembresia.registrar(socioId, tipo);
    return new ModelAndView("redirect:/membresias");
  }
}
