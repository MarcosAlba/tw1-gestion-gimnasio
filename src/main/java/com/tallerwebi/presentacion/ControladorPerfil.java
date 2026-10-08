package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.EdadInvalida;
import com.tallerwebi.dominio.excepcion.FotoDemasiadoGrande;
import com.tallerwebi.dominio.excepcion.FotoInvalida;
import com.tallerwebi.dominio.interfaces.ServicioPerfil;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ControladorPerfil {

  private static final String VISTA_PERFIL = "perfil";
  private static final String VISTA_EDITAR = "perfil-editar";
  private static final String DATOS_PERFIL = "datosPerfil";
  private static final String REDIRECT_LOGIN = "redirect:/login";
  private static final String REDIRECT_PERFIL = "redirect:/perfil";

  private ServicioPerfil servicioPerfil;

  @Autowired
  public ControladorPerfil(ServicioPerfil servicioPerfil) {
    this.servicioPerfil = servicioPerfil;
  }

  // Pantalla de lectura: muestra cómo quedó el perfil
  @RequestMapping(path = "/perfil", method = RequestMethod.GET)
  public ModelAndView verPerfil(HttpServletRequest request) {
    Long usuarioId = usuarioDeLaSesion(request);
    if (usuarioId == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    Map<String, Object> modelo = new ModelMap();
    modelo.put("usuario", servicioPerfil.obtener(usuarioId));
    return new ModelAndView(VISTA_PERFIL, modelo);
  }

  // Formulario de edición, con lo que el usuario ya tiene cargado
  @RequestMapping(path = "/perfil/editar", method = RequestMethod.GET)
  public ModelAndView editarPerfil(HttpServletRequest request) {
    Long usuarioId = usuarioDeLaSesion(request);
    if (usuarioId == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }
    Usuario usuario = servicioPerfil.obtener(usuarioId);
    return formulario(usuario, datosDe(usuario), null);
  }

  @RequestMapping(path = "/perfil/guardar", method = RequestMethod.POST)
  public ModelAndView guardarPerfil(
    @ModelAttribute(DATOS_PERFIL) DatosPerfil datos,
    HttpServletRequest request,
    RedirectAttributes redirect
  ) {
    // El usuario sale siempre de la sesión, nunca del formulario
    Long usuarioId = usuarioDeLaSesion(request);
    if (usuarioId == null) {
      return new ModelAndView(REDIRECT_LOGIN);
    }

    try {
      MultipartFile foto = datos.getFoto();
      servicioPerfil.actualizar(
        usuarioId,
        cambiosDe(datos),
        bytesDe(foto),
        foto == null ? null : foto.getContentType()
      );
    } catch (FotoDemasiadoGrande e) {
      return volverAlFormulario(
        usuarioId,
        datos,
        "La foto pesa más de 2 MB. Elegí una más liviana."
      );
    } catch (FotoInvalida e) {
      return volverAlFormulario(usuarioId, datos, "La foto tiene que ser JPG, PNG o WebP.");
    } catch (EdadInvalida e) {
      return volverAlFormulario(usuarioId, datos, "La edad tiene que estar entre 1 y 120.");
    } catch (IOException e) {
      return volverAlFormulario(usuarioId, datos, "No pudimos leer la foto. Probá con otra.");
    }

    redirect.addFlashAttribute("exito", "Perfil actualizado.");
    return new ModelAndView(REDIRECT_PERFIL);
  }

  private Long usuarioDeLaSesion(HttpServletRequest request) {
    if (request.getSession().getAttribute("ROL") == null) {
      return null;
    }
    return (Long) request.getSession().getAttribute("ID_USUARIO");
  }

  private byte[] bytesDe(MultipartFile foto) throws IOException {
    return foto == null ? null : foto.getBytes();
  }

  private ModelAndView volverAlFormulario(Long usuarioId, DatosPerfil datos, String error) {
    return formulario(servicioPerfil.obtener(usuarioId), datos, error);
  }

  private ModelAndView formulario(Usuario usuario, DatosPerfil datos, String error) {
    Map<String, Object> modelo = new ModelMap();
    modelo.put("usuario", usuario);
    modelo.put(DATOS_PERFIL, datos);
    if (error != null) {
      modelo.put("error", error);
    }
    return new ModelAndView(VISTA_EDITAR, modelo);
  }

  private DatosPerfil datosDe(Usuario usuario) {
    DatosPerfil datos = new DatosPerfil();
    datos.setNombre(usuario.getNombre());
    datos.setApellido(usuario.getApellido());
    datos.setEdad(usuario.getEdad());
    datos.setDeporte(usuario.getDeporte());
    return datos;
  }

  private Usuario cambiosDe(DatosPerfil datos) {
    Usuario cambios = new Usuario();
    cambios.setNombre(datos.getNombre());
    cambios.setApellido(datos.getApellido());
    cambios.setEdad(datos.getEdad());
    cambios.setDeporte(datos.getDeporte());
    return cambios;
  }
}
