package com.tallerwebi.dominio;

import com.tallerwebi.dominio.enums.DiaSemana;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.interfaces.RepositorioItemRutina;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioRutina;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioRutinaImpl implements ServicioRutina {

  private final RepositorioUsuario repositorioUsuario;
  private final RepositorioItemRutina repositorioItemRutina;

  @Autowired
  public ServicioRutinaImpl(
    RepositorioUsuario repositorioUsuario,
    RepositorioItemRutina repositorioItemRutina
  ) {
    this.repositorioUsuario = repositorioUsuario;
    this.repositorioItemRutina = repositorioItemRutina;
  }

  @Override
  public List<Usuario> buscarSocios(String criterio) {
    return repositorioUsuario.buscarSocios(criterio);
  }

  @Override
  public Usuario obtenerSocio(Long idSocio) throws UsuarioNoEncontrado {
    Usuario socio = repositorioUsuario.buscarPorId(idSocio);
    if (socio == null) {
      throw new UsuarioNoEncontrado();
    }
    return socio;
  }

  @Override
  public void asignarEjercicio(Long idSocio, Long idEntrenador, ItemRutina item)
    throws UsuarioNoEncontrado {
    Usuario socio = obtenerSocio(idSocio);
    Usuario entrenador = repositorioUsuario.buscarPorId(idEntrenador);
    item.setSocio(socio);
    item.setEntrenador(entrenador);
    repositorioItemRutina.guardar(item);
  }

  @Override
  public Map<DiaSemana, List<ItemRutina>> obtenerRutinaPorDias(Long idSocio) {
    Map<DiaSemana, List<ItemRutina>> rutinaPorDia = new LinkedHashMap<>();
    for (DiaSemana dia : DiaSemana.values()) {
      rutinaPorDia.put(dia, new ArrayList<>());
    }

    List<ItemRutina> items = repositorioItemRutina.buscarPorSocio(idSocio);
    for (ItemRutina it : items) {
      if (it.getDia() != null) {
        rutinaPorDia.get(it.getDia()).add(it);
      }
    }
    return rutinaPorDia;
  }

  @Override
  public void eliminarEjercicio(Long idItem) {
    repositorioItemRutina.eliminar(idItem);
  }

  @Override
  public void registrarAvance(Long idItem, Long idSocio, Double pesoReal) {
    ItemRutina item = repositorioItemRutina.buscarPorId(idItem);
    if (item != null && item.getSocio() != null && item.getSocio().getId().equals(idSocio)) {
      if (item.getDetalle() == null) {
        item.setDetalle(new DetalleEjercicio());
      }
      item.getDetalle().setCompletado(true);
      if (pesoReal != null && pesoReal > 0) {
        item.getDetalle().setPesoLevantadoKg(pesoReal);
      }
    }
  }
}
