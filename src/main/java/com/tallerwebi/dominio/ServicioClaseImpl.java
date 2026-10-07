package com.tallerwebi.dominio;

import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.excepcion.EntrenadorInvalido;
import com.tallerwebi.dominio.interfaces.RepositorioClase;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioClase;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioClase")
@Transactional
public class ServicioClaseImpl implements ServicioClase {

  private static final int DIAS_DE_LA_SEMANA = 7;

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

  @Override
  public Map<LocalDate, List<Clase>> listarSemana(CapacidadFisica capacidad) {
    LocalDate hoy = LocalDate.now();

    // Un día por columna, en orden de fecha y con la lista vacía si no hay clases
    Map<LocalDate, List<Clase>> semana = new LinkedHashMap<>();
    for (int i = 0; i < DIAS_DE_LA_SEMANA; i++) {
      semana.put(hoy.plusDays(i), new ArrayList<>());
    }

    for (Clase clase : repoClase.buscarDesde(hoy.atStartOfDay())) {
      boolean coincideConElFiltro = capacidad == null || clase.getCapacidad() == capacidad;
      List<Clase> delDia = semana.get(clase.getInicio().toLocalDate());
      if (coincideConElFiltro && delDia != null) {
        delDia.add(clase);
      }
    }

    for (List<Clase> delDia : semana.values()) {
      delDia.sort(Comparator.comparing(Clase::getInicio));
    }
    return semana;
  }
}
