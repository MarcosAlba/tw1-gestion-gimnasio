package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.EjercicioApi;
import java.util.List;
import java.util.Set;

public interface ServicioCatalogoApi {
  List<EjercicioApi> obtenerEjercicios(String query, String equipment, String group);
  Set<String> obtenerEquipamientos();
  Set<String> obtenerGrupos();
  EjercicioApi buscarPorSlug(String slug);
}
