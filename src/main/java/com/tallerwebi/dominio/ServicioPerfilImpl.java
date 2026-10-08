package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EdadInvalida;
import com.tallerwebi.dominio.excepcion.FotoDemasiadoGrande;
import com.tallerwebi.dominio.excepcion.FotoInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioPerfil;
import jakarta.transaction.Transactional;
import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioPerfil")
@Transactional
public class ServicioPerfilImpl implements ServicioPerfil {

  private static final int TAMANIO_MAXIMO_FOTO = 2 * 1024 * 1024;
  private static final int EDAD_MINIMA = 1;
  private static final int EDAD_MAXIMA = 120;
  private static final String ROL_SOCIO = "SOCIO";
  private static final List<String> TIPOS_PERMITIDOS = List.of(
    "image/jpeg",
    "image/png",
    "image/webp"
  );

  private RepositorioUsuario repoUsuario;

  @Autowired
  public ServicioPerfilImpl(RepositorioUsuario repoUsuario) {
    this.repoUsuario = repoUsuario;
  }

  @Override
  public Usuario obtener(Long usuarioId) {
    Usuario usuario = repoUsuario.buscarPorId(usuarioId);
    if (usuario == null) {
      throw new UsuarioNoEncontrado();
    }
    return usuario;
  }

  @Override
  public void actualizar(Long usuarioId, Usuario cambios, byte[] foto, String tipoFoto)
    throws FotoDemasiadoGrande, FotoInvalida, EdadInvalida {
    Usuario usuario = obtener(usuarioId);

    // Todo se valida antes de tocar el usuario: si algo falla, no se modifica nada
    validarEdad(cambios.getEdad());
    String fotoNueva = null;
    if (foto != null && foto.length > 0) {
      fotoNueva = convertirFoto(foto, tipoFoto);
    }

    // Campo por campo: nunca se copian el rol, el email ni la contraseña
    usuario.setNombre(cambios.getNombre());
    usuario.setApellido(cambios.getApellido());
    usuario.setEdad(cambios.getEdad());
    if (fotoNueva != null) {
      usuario.setFotoPerfil(fotoNueva);
    }
    // El entrenador no ve el campo deporte, así que solo el socio lo cambia
    if (ROL_SOCIO.equals(usuario.getRol())) {
      usuario.setDeporte(cambios.getDeporte());
    }

    repoUsuario.modificar(usuario);
  }

  private void validarEdad(Integer edad) {
    if (edad != null && (edad < EDAD_MINIMA || edad > EDAD_MAXIMA)) {
      throw new EdadInvalida();
    }
  }

  private String convertirFoto(byte[] foto, String tipo) throws FotoDemasiadoGrande {
    if (foto.length > TAMANIO_MAXIMO_FOTO) {
      throw new FotoDemasiadoGrande();
    }
    if (tipo == null || !TIPOS_PERMITIDOS.contains(tipo)) {
      throw new FotoInvalida();
    }
    return "data:" + tipo + ";base64," + Base64.getEncoder().encodeToString(foto);
  }
}
