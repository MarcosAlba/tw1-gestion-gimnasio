package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EntrenadorInvalido;
import com.tallerwebi.dominio.interfaces.RepositorioClase;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioClase;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioClase")
@Transactional
public class ServicioClaseImpl implements ServicioClase {

  private RepositorioClase repoClase;
  private RepositorioUsuario repoUsuario;

  @Autowired
  public ServicioClaseImpl(RepositorioClase repoClase, RepositorioUsuario repoUsuario) {
    this.repoClase = repoClase;
    this.repoUsuario = repoUsuario;
  }

  @Override
  public void crear(Clase clase, Long entrenadorId) throws EntrenadorInvalido {
    Usuario entrenador = repoUsuario.buscarPorId(entrenadorId);
    if (entrenador == null || !"ENTRENADOR".equals(entrenador.getRol())) {
      throw new EntrenadorInvalido();
    }
    clase.setEntrenador(entrenador);
    repoClase.guardar(clase);
  }

  @Override
  public List<Clase> listarProximas() {
    return repoClase.buscarDesde(LocalDateTime.now());
  }
}
