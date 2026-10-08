package com.tallerwebi.dominio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tallerwebi.dominio.EjercicioApi;
import com.tallerwebi.dominio.interfaces.ServicioCatalogoApi;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service("servicioCatalogoApi")
public class ServicioCatalogoApiImpl implements ServicioCatalogoApi {

  private static final String ARCHIVO_DATASET = "exercise-api-dataset.json";
  private final ObjectMapper objectMapper = new ObjectMapper();
  private List<EjercicioApi> cacheEjercicios = null;

  private List<EjercicioApi> cargarTodos() {
    if (cacheEjercicios != null && !cacheEjercicios.isEmpty()) {
      return cacheEjercicios;
    }

    try (InputStream stream = new ClassPathResource(ARCHIVO_DATASET).getInputStream()) {
      JsonNode nodoRaiz = objectMapper.readTree(stream);
      cacheEjercicios = extraerListaEjercicios(nodoRaiz);
    } catch (Exception ignored) {
      cacheEjercicios = Collections.emptyList();
    }

    return cacheEjercicios;
  }

  private List<EjercicioApi> extraerListaEjercicios(JsonNode nodoRaiz) {
    if (nodoRaiz == null) {
      return Collections.emptyList();
    }

    JsonNode nodoContenedor = buscarNodoContenedor(nodoRaiz);

    if (nodoContenedor.isArray()) {
      return procesarArreglo(nodoContenedor);
    }

    return procesarObjetoMapa(nodoContenedor);
  }

  private JsonNode buscarNodoContenedor(JsonNode nodoRaiz) {
    if (nodoRaiz.has("exercises")) {
      return nodoRaiz.get("exercises");
    }
    if (nodoRaiz.has("items")) {
      return nodoRaiz.get("items");
    }
    if (nodoRaiz.has("data")) {
      return nodoRaiz.get("data");
    }
    return nodoRaiz;
  }

  private List<EjercicioApi> procesarArreglo(JsonNode nodoArreglo) {
    List<EjercicioApi> resultado = new ArrayList<>();
    for (JsonNode item : nodoArreglo) {
      EjercicioApi ejercicio = convertirNodoAEjercicio(item);
      if (ejercicio != null) {
        resultado.add(ejercicio);
      }
    }
    return resultado;
  }

  private List<EjercicioApi> procesarObjetoMapa(JsonNode nodoMapa) {
    List<EjercicioApi> resultado = new ArrayList<>();
    Iterator<JsonNode> elementos = nodoMapa.elements();
    while (elementos.hasNext()) {
      JsonNode item = elementos.next();
      EjercicioApi ejercicio = convertirNodoAEjercicio(item);
      if (ejercicio != null) {
        resultado.add(ejercicio);
      }
    }
    return resultado;
  }

  private EjercicioApi convertirNodoAEjercicio(JsonNode nodo) {
    try {
      EjercicioApi ejercicio = objectMapper.treeToValue(nodo, EjercicioApi.class);
      if (ejercicio != null && ejercicio.getName() != null) {
        return ejercicio;
      }
    } catch (Exception ignored) {
      // Ignora elementos no compatibles
    }
    return null;
  }

  @Override
  public List<EjercicioApi> obtenerEjercicios(String query, String equipment, String group) {
    List<EjercicioApi> todos = cargarTodos();

    return todos
      .stream()
      .filter(ejercicio -> coincideNombre(ejercicio, query))
      .filter(ejercicio -> coincideEquipamiento(ejercicio, equipment))
      .filter(ejercicio -> coincideGrupo(ejercicio, group))
      .collect(Collectors.toList());
  }

  private boolean coincideNombre(EjercicioApi ejercicio, String query) {
    if (query == null || query.isBlank()) {
      return true;
    }
    String texto = query.trim().toLowerCase(Locale.ROOT);
    String nombreEs = ejercicio.getName() != null && ejercicio.getName().getEs() != null
      ? ejercicio.getName().getEs().toLowerCase(Locale.ROOT)
      : "";
    String nombreEn = ejercicio.getName() != null && ejercicio.getName().getEn() != null
      ? ejercicio.getName().getEn().toLowerCase(Locale.ROOT)
      : "";
    return nombreEs.contains(texto) || nombreEn.contains(texto);
  }

  private boolean coincideEquipamiento(EjercicioApi ejercicio, String equipment) {
    if (equipment == null || equipment.isBlank()) {
      return true;
    }
    return (
      ejercicio.getEquipment() != null &&
      equipment.equalsIgnoreCase(ejercicio.getEquipment().getEs())
    );
  }

  private boolean coincideGrupo(EjercicioApi ejercicio, String group) {
    if (group == null || group.isBlank()) {
      return true;
    }
    return ejercicio.getGroup() != null && group.equalsIgnoreCase(ejercicio.getGroup().getEs());
  }

  @Override
  public Set<String> obtenerEquipamientos() {
    return cargarTodos()
      .stream()
      .filter(ejercicio ->
        ejercicio.getEquipment() != null && ejercicio.getEquipment().getEs() != null
      )
      .map(ejercicio -> ejercicio.getEquipment().getEs())
      .collect(Collectors.toCollection(TreeSet::new));
  }

  @Override
  public Set<String> obtenerGrupos() {
    return cargarTodos()
      .stream()
      .filter(ejercicio -> ejercicio.getGroup() != null && ejercicio.getGroup().getEs() != null)
      .map(ejercicio -> ejercicio.getGroup().getEs())
      .collect(Collectors.toCollection(TreeSet::new));
  }

  @Override
  public EjercicioApi buscarPorSlug(String slug) {
    if (slug == null || slug.isBlank()) {
      return null;
    }
    return cargarTodos()
      .stream()
      .filter(ejercicio -> slug.equalsIgnoreCase(ejercicio.getSlug()))
      .findFirst()
      .orElse(null);
  }
}
