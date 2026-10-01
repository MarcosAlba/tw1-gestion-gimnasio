package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.Ejercicio;
import com.tallerwebi.dominio.excepcion.EjercicioExistente;
import com.tallerwebi.dominio.excepcion.NombreEjercicioInvalido;
import java.util.List;

public interface ServicioEjercicio {
  void registrar(Ejercicio ejercicio) throws NombreEjercicioInvalido, EjercicioExistente;
  List<Ejercicio> listarTodos();
}
