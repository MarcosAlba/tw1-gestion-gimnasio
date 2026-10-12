package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Clase;
import com.tallerwebi.dominio.Reserva;
import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.excepcion.ClaseSuperpuesta;
import com.tallerwebi.dominio.excepcion.EntrenadorInvalido;
import com.tallerwebi.dominio.excepcion.FechaClaseInvalida;
import com.tallerwebi.dominio.excepcion.LugarOcupado;
import com.tallerwebi.dominio.interfaces.ServicioClase;
import com.tallerwebi.dominio.interfaces.ServicioReserva;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorClase {

  private static final String ATRIBUTO_DATOS_CLASE = "datosClase";
  private static final String ATRIBUTO_ROL = "ROL";
  private static final String ATRIBUTO_ERROR = "error";
  private static final String ROL_ENTRENADOR = "ENTRENADOR";

  private final ServicioClase servicioClase;
  private final ServicioReserva servicioReserva;

  @Autowired
  public ControladorClase(ServicioClase servicioClase, ServicioReserva servicioReserva) {
    this.servicioClase = servicioClase;
    this.servicioReserva = servicioReserva;
  }

  @GetMapping("/clases/nueva")
  public ModelAndView irANuevaClase(
    @RequestParam(value = "fecha", required = false) @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE
    ) LocalDate fecha,
    HttpServletRequest request
  ) {
    if (!esEntrenador(request)) {
      return new ModelAndView("redirect:/login");
    }

    DatosClase datosClase = new DatosClase();
    if (fecha != null) {
      datosClase.setInicio(fecha.atTime(9, 0));
    }

    ModelAndView modelAndView = new ModelAndView("nueva-clase");
    modelAndView.addObject(ATRIBUTO_DATOS_CLASE, datosClase);
    return modelAndView;
  }

  @PostMapping("/clases/guardar")
  public ModelAndView guardarClase(
    @ModelAttribute(ATRIBUTO_DATOS_CLASE) DatosClase datosClase,
    HttpServletRequest request
  ) {
    if (!esEntrenador(request)) {
      return new ModelAndView("redirect:/login");
    }

    Long idEntrenador = (Long) request.getSession().getAttribute("ID_USUARIO");

    try {
      Clase clase = new Clase();
      clase.setNombre(datosClase.getNombre());
      clase.setInicio(datosClase.getInicio());
      clase.setDuracion(datosClase.getDuracion());
      clase.setLugar(datosClase.getLugar());
      clase.setCupo(datosClase.getCupo());
      clase.setCapacidad(datosClase.getCapacidad());

      servicioClase.crear(clase, idEntrenador);
      return new ModelAndView("redirect:/clases");
    } catch (FechaClaseInvalida exFecha) {
      return volverAlFormulario(
        datosClase,
        "La fecha y hora no pueden ser anteriores al momento actual"
      );
    } catch (EntrenadorInvalido exEntrenador) {
      return volverAlFormulario(datosClase, "El usuario no es un entrenador valido");
    } catch (ClaseSuperpuesta exSuperpuesta) {
      return volverAlFormulario(
        datosClase,
        "Ya tenés una clase asignada que se superpone con este horario"
      );
    } catch (LugarOcupado exLugar) {
      return volverAlFormulario(
        datosClase,
        "El lugar ya se encuentra ocupado en ese horario por otra clase"
      );
    }
  }

  @GetMapping("/clases")
  public ModelAndView listarClases(HttpServletRequest request) {
    HttpSession sesion = request.getSession();
    if (sesion.getAttribute(ATRIBUTO_ROL) == null) {
      return new ModelAndView("redirect:/login");
    }

    List<Clase> clases = servicioClase.listarProximas();
    ModelAndView modelAndView = new ModelAndView("clases");
    modelAndView.addObject("clases", clases);
    return modelAndView;
  }

  @GetMapping("/horarios")
  public ModelAndView verHorarios(
    @RequestParam(value = "capacidad", required = false) String capacidad,
    HttpServletRequest request
  ) {
    CapacidadFisica filtro = null;
    if (capacidad != null && !capacidad.isBlank()) {
      filtro = CapacidadFisica.valueOf(capacidad.toUpperCase(Locale.ROOT));
    }

    Map<LocalDate, List<Clase>> semana = servicioClase.listarSemana(filtro);
    ModelAndView modelAndView = new ModelAndView("horarios");
    modelAndView.addObject("clasesPorDia", semana);
    modelAndView.addObject("capacidadSeleccionada", filtro);

    HttpSession sesion = request != null ? request.getSession(false) : null;
    if (sesion != null && "SOCIO".equals(sesion.getAttribute("ROL"))) {
      Long idSocio = (Long) sesion.getAttribute("ID_USUARIO");
      List<Reserva> misReservas = servicioReserva.misReservas(idSocio);
      List<Long> clasesReservadasIds = misReservas
        .stream()
        .filter(r -> !"CANCELADA".equals(r.getEstado().name()))
        .map(r -> r.getClase().getId())
        .toList();
      modelAndView.addObject("clasesReservadasIds", clasesReservadasIds);
    }

    return modelAndView;
  }

  private boolean esEntrenador(HttpServletRequest request) {
    HttpSession sesion = request.getSession();
    String rol = (String) sesion.getAttribute(ATRIBUTO_ROL);
    return ROL_ENTRENADOR.equals(rol);
  }

  private ModelAndView volverAlFormulario(DatosClase datosClase, String mensajeError) {
    ModelAndView modelAndView = new ModelAndView("nueva-clase");
    modelAndView.addObject(ATRIBUTO_ERROR, mensajeError);
    modelAndView.addObject(ATRIBUTO_DATOS_CLASE, datosClase);
    return modelAndView;
  }
}
