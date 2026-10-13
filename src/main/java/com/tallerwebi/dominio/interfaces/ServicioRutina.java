package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.ItemRutina;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.enums.DiaSemana;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import java.util.List;
import java.util.Map;

public interface ServicioRutina {
  List<Usuario> buscarSocios(String criterio);
  Usuario obtenerSocio(Long idSocio) throws UsuarioNoEncontrado;
  void asignarEjercicio(Long idSocio, Long idEntrenador, ItemRutina item)
    throws UsuarioNoEncontrado;
  Map<DiaSemana, List<ItemRutina>> obtenerRutinaPorDias(Long idSocio);
  void eliminarEjercicio(Long idItem);
  void registrarAvance(Long idItem, Long idSocio, Double pesoReal);
}
