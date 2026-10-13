package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.ItemRutina;
import java.util.List;

public interface RepositorioItemRutina {
  void guardar(ItemRutina item);
  List<ItemRutina> buscarPorSocio(Long idSocio);
  void eliminar(Long idItem);
  ItemRutina buscarPorId(Long idItem);
}
