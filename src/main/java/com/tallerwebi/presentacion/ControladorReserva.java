package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.excepcion.ClaseNoEncontrada;
import com.tallerwebi.dominio.excepcion.ClaseSinCupo;
import com.tallerwebi.dominio.excepcion.MembresiaNoVigente;
import com.tallerwebi.dominio.excepcion.ReservaDuplicada;
import com.tallerwebi.dominio.excepcion.ReservaNoEncontrada;
import com.tallerwebi.dominio.interfaces.ServicioReserva;
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
public class ControladorReserva {

  private static final String VISTA_RESERVAS = "reservas";
  private static final String REDIRECT_LOGIN = "redirect:/login";
  private static final String REDIRECT_RESERVAS = "redirect:/reservas";

  private ServicioReserva servicioReserva;

  @Autowired
  public ControladorReserva(ServicioReserva servicioReserva) {
    this.servicioReserva = servicioReserva;
  }

  @RequestMapping(path = "/reservas", method = RequestMethod.GET)
  public ModelAndView verReservas(HttpServletRequest request) {
    if (!esSocio(request)) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    return vistaReservas(idSocio(request), null);
  }

  @RequestMapping(path = "/reservas/reservar", method = RequestMethod.POST)
  public ModelAndView reservar(@RequestParam("claseId") Long claseId, HttpServletRequest request) {
    if (!esSocio(request)) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    Long socioId = idSocio(request);

    try {
      servicioReserva.reservar(socioId, claseId);
    } catch (MembresiaNoVigente e) {
      return vistaReservas(socioId, "No tenés una membresía vigente");
    } catch (ReservaDuplicada e) {
      return vistaReservas(socioId, "Ya reservaste esta clase");
    } catch (ClaseSinCupo e) {
      return vistaReservas(socioId, "La clase no tiene cupos disponibles");
    } catch (ClaseNoEncontrada e) {
      return vistaReservas(socioId, "La clase no existe");
    }
    return new ModelAndView(REDIRECT_RESERVAS);
  }

  @RequestMapping(path = "/reservas/cancelar", method = RequestMethod.POST)
  public ModelAndView cancelar(
    @RequestParam("reservaId") Long reservaId,
    HttpServletRequest request
  ) {
    if (!esSocio(request)) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    Long socioId = idSocio(request);

    try {
      servicioReserva.cancelar(socioId, reservaId);
    } catch (ReservaNoEncontrada e) {
      return vistaReservas(socioId, "No se encontró la reserva");
    }
    return new ModelAndView(REDIRECT_RESERVAS);
  }

  private boolean esSocio(HttpServletRequest request) {
    return "SOCIO".equals(request.getSession().getAttribute("ROL"));
  }

  private Long idSocio(HttpServletRequest request) {
    return (Long) request.getSession().getAttribute("ID_USUARIO");
  }

  private ModelAndView vistaReservas(Long socioId, String error) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("reservas", servicioReserva.misReservas(socioId));
    if (error != null) {
      modelo.put("error", error);
    }
    return new ModelAndView(VISTA_RESERVAS, modelo);
  }
}
