package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.RolInvalido;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.interfaces.ServicioLogin;
import jakarta.servlet.http.HttpServletRequest;
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
public class ControladorLogin {

  private static final String VISTA_NUEVO_USUARIO = "nuevo-usuario";
  private static final String CLAVE_ERROR = "error";
  private static final String CLAVE_USUARIO = "usuario";

  private ServicioLogin servicioLogin;

  @Autowired
  public ControladorLogin(ServicioLogin servicioLogin) {
    this.servicioLogin = servicioLogin;
  }

  @RequestMapping("/login")
  public ModelAndView irALogin() {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("datosLogin", new DatosLogin());
    return new ModelAndView("login", modelo);
  }

  @RequestMapping(path = "/validar-login", method = RequestMethod.POST)
  public ModelAndView validarLogin(
    @ModelAttribute("datosLogin") DatosLogin datosLogin,
    HttpServletRequest request,
    @RequestParam(name = "volver", required = false) String volver
  ) {
    Usuario usuarioBuscado = servicioLogin.consultarUsuario(
      datosLogin.getEmail(),
      datosLogin.getPassword()
    );
    if (usuarioBuscado != null) {
      request.getSession().setAttribute("ROL", usuarioBuscado.getRol());
      request.getSession().setAttribute("ID_USUARIO", usuarioBuscado.getId());

      if (volver != null && volver.startsWith("/") && !volver.startsWith("//")) {
        return new ModelAndView("redirect:" + volver);
      }
      return new ModelAndView("redirect:/home");
    } else {
      Map<String, Object> model = new ModelMap();
      model.put(CLAVE_ERROR, "Usuario o clave incorrecta");
      return new ModelAndView("login", model);
    }
  }

  @RequestMapping(path = "/registrarme", method = RequestMethod.POST)
  public ModelAndView registrarme(@ModelAttribute(CLAVE_USUARIO) Usuario usuario) {
    Map<String, Object> model = new ModelMap();
    // El formulario se vuelve a mostrar con lo que el usuario ya escribió
    model.put(CLAVE_USUARIO, usuario);
    try {
      servicioLogin.registrar(usuario);
    } catch (UsuarioExistente e) {
      model.put(CLAVE_ERROR, "El usuario ya existe");
      return new ModelAndView(VISTA_NUEVO_USUARIO, model);
    } catch (RolInvalido e) {
      model.put(CLAVE_ERROR, "El rol elegido no es válido");
      return new ModelAndView(VISTA_NUEVO_USUARIO, model);
    } catch (Exception e) {
      model.put(CLAVE_ERROR, "Error al registrar el nuevo usuario");
      return new ModelAndView(VISTA_NUEVO_USUARIO, model);
    }
    return new ModelAndView("redirect:/login");
  }

  @RequestMapping(path = "/nuevo-usuario", method = RequestMethod.GET)
  public ModelAndView nuevoUsuario() {
    Map<String, Object> model = new ModelMap();
    Usuario usuario = new Usuario();
    usuario.setRol("SOCIO");
    model.put(CLAVE_USUARIO, usuario);
    return new ModelAndView(VISTA_NUEVO_USUARIO, model);
  }

  @RequestMapping(path = "/home", method = RequestMethod.GET)
  public ModelAndView irAHome() {
    return new ModelAndView("home");
  }

  @RequestMapping(path = "/", method = RequestMethod.GET)
  public ModelAndView inicio(HttpServletRequest request) {
    if (request.getSession().getAttribute("ROL") != null) {
      return new ModelAndView("redirect:/home");
    } else {
      return new ModelAndView("bienvenida");
    }
  }
}
