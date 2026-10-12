package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Reserva;
import com.tallerwebi.dominio.excepcion.ClaseNoEncontrada;
import com.tallerwebi.dominio.excepcion.MembresiaNoVigente;
import com.tallerwebi.dominio.excepcion.ReservaDuplicada;
import com.tallerwebi.dominio.excepcion.ReservaNoEncontrada;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.interfaces.ServicioReserva;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorReserva {

  private static final String ATRIBUTO_ROL = "ROL";
  private static final String ATRIBUTO_ID_USUARIO = "ID_USUARIO";
  private static final String ATRIBUTO_ERROR = "error";
  private static final String ATRIBUTO_EXITO = "exito";
  private static final String ROL_SOCIO = "SOCIO";
  private static final String ORIGEN_HORARIOS = "horarios";
  private static final String VISTA_RESERVAS = "reservas";
  private static final String REDIRECT_LOGIN = "redirect:/login";
  private static final String REDIRECT_HORARIOS = "redirect:/horarios";
  private static final String REDIRECT_RESERVAS = "redirect:/reservas";

  private ServicioReserva servicioReserva;

  @Autowired
  public ControladorReserva(ServicioReserva servicioReserva) {
    this.servicioReserva = servicioReserva;
  }

  @GetMapping("/reservas")
  public ModelAndView verReservas(HttpServletRequest request) {
    if (!esSocio(request)) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    Long idSocio = (Long) request.getSession().getAttribute(ATRIBUTO_ID_USUARIO);
    List<Reserva> reservas = servicioReserva.misReservas(idSocio);

    ModelAndView modelAndView = new ModelAndView(VISTA_RESERVAS);
    modelAndView.addObject(VISTA_RESERVAS, reservas);
    return modelAndView;
  }

  @PostMapping("/reservas/reservar")
  public ModelAndView reservar(
    @RequestParam("claseId") Long claseId,
    @RequestParam(value = "origen", required = false) String origen,
    HttpServletRequest request,
    RedirectAttributes redirectAttrs
  ) {
    if (!esSocio(request)) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    Long idSocio = (Long) request.getSession().getAttribute(ATRIBUTO_ID_USUARIO);
    boolean vieneDeHorarios = ORIGEN_HORARIOS.equals(origen);

    try {
      servicioReserva.reservar(idSocio, claseId);
      if (vieneDeHorarios) {
        redirectAttrs.addFlashAttribute(ATRIBUTO_EXITO, "¡Reserva realizada con éxito!");
        return new ModelAndView(REDIRECT_HORARIOS);
      }
      return new ModelAndView(REDIRECT_RESERVAS);
    } catch (MembresiaNoVigente exMembresia) {
      return manejarErrorReserva(
        vieneDeHorarios,
        "No tenés una membresía vigente",
        redirectAttrs,
        idSocio
      );
    } catch (ReservaDuplicada exDuplicada) {
      return manejarErrorReserva(
        vieneDeHorarios,
        "Ya tenés un lugar o estás en lista de espera para esta clase",
        redirectAttrs,
        idSocio
      );
    } catch (ClaseNoEncontrada exClase) {
      return manejarErrorReserva(vieneDeHorarios, "La clase no existe", redirectAttrs, idSocio);
    } catch (UsuarioNoEncontrado exUsuario) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
  }

  @PostMapping("/reservas/cancelar")
  public ModelAndView cancelar(
    @RequestParam("reservaId") Long reservaId,
    HttpServletRequest request
  ) {
    if (!esSocio(request)) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    Long idSocio = (Long) request.getSession().getAttribute(ATRIBUTO_ID_USUARIO);
    ModelAndView modelAndView = new ModelAndView(VISTA_RESERVAS);

    try {
      servicioReserva.cancelar(idSocio, reservaId);
      return new ModelAndView(REDIRECT_RESERVAS);
    } catch (ReservaNoEncontrada exReserva) {
      modelAndView.addObject(ATRIBUTO_ERROR, "No se encontró la reserva");
    }

    modelAndView.addObject(VISTA_RESERVAS, servicioReserva.misReservas(idSocio));
    return modelAndView;
  }

  private boolean esSocio(HttpServletRequest request) {
    HttpSession sesion = request.getSession();
    String rol = (String) sesion.getAttribute(ATRIBUTO_ROL);
    return ROL_SOCIO.equals(rol);
  }

  private ModelAndView manejarErrorReserva(
    boolean vieneDeHorarios,
    String mensaje,
    RedirectAttributes redirectAttrs,
    Long idSocio
  ) {
    if (vieneDeHorarios) {
      redirectAttrs.addFlashAttribute(ATRIBUTO_ERROR, mensaje);
      return new ModelAndView(REDIRECT_HORARIOS);
    }
    ModelAndView modelAndView = new ModelAndView(VISTA_RESERVAS);
    modelAndView.addObject(ATRIBUTO_ERROR, mensaje);
    modelAndView.addObject(VISTA_RESERVAS, servicioReserva.misReservas(idSocio));
    return modelAndView;
  }
}
