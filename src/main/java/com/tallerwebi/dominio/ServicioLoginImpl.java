package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.RolInvalido;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioLogin;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioLogin")
@Transactional
public class ServicioLoginImpl implements ServicioLogin {

  private static final String ROL_SOCIO = "SOCIO";
  private static final String ROL_ENTRENADOR = "ENTRENADOR";

  private RepositorioUsuario repositorioUsuario;

  @Autowired
  public ServicioLoginImpl(RepositorioUsuario repositorioUsuario) {
    this.repositorioUsuario = repositorioUsuario;
  }

  @Override
  public Usuario consultarUsuario(String email, String password) {
    return repositorioUsuario.buscarUsuario(email, password);
  }

  @Override
  public void registrar(Usuario usuario) throws UsuarioExistente, RolInvalido {
    if (!esRolPermitido(usuario.getRol())) {
      throw new RolInvalido();
    }
    if (repositorioUsuario.buscar(usuario.getEmail()) != null) {
      throw new UsuarioExistente();
    }
    if (ROL_ENTRENADOR.equals(usuario.getRol())) {
      usuario.setDeporte(null);
    }
    repositorioUsuario.guardar(usuario);
  }

  private boolean esRolPermitido(String rol) {
    return ROL_SOCIO.equals(rol) || ROL_ENTRENADOR.equals(rol);
  }
}
