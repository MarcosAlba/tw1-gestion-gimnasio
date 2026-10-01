package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.EjercicioExistente;
import com.tallerwebi.dominio.excepcion.NombreEjercicioInvalido;
import com.tallerwebi.dominio.interfaces.RepositorioEjercicio;
import com.tallerwebi.dominio.interfaces.ServicioEjercicio;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioEjercicio")
@Transactional
public class ServicioEjercicioImpl implements ServicioEjercicio {

  private RepositorioEjercicio repoEjercicio;

  @Autowired
  public ServicioEjercicioImpl(RepositorioEjercicio repoEjercicio) {
    this.repoEjercicio = repoEjercicio;
  }

  @Override
  public void registrar(Ejercicio ejercicio) throws NombreEjercicioInvalido, EjercicioExistente {
    if (ejercicio.getNombre() == null || ejercicio.getNombre().isBlank()) {
      throw new NombreEjercicioInvalido();
    }

    if (repoEjercicio.buscarPorNombre(ejercicio.getNombre()) != null) {
      throw new EjercicioExistente();
    }

    repoEjercicio.guardar(ejercicio);
  }

  @Override
  public List<Ejercicio> listarTodos() {
    return repoEjercicio.buscarTodos();
  }
}
