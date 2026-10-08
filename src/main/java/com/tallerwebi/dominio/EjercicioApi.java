package com.tallerwebi.dominio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tallerwebi.presentacion.DatosImagenesApi;
import com.tallerwebi.presentacion.DatosItemApi;
import com.tallerwebi.presentacion.DatosTextosApi;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EjercicioApi {

  private String slug;
  private DatosItemApi name;
  private DatosItemApi group;
  private DatosItemApi equipment;
  private List<DatosItemApi> primaryMuscles;
  private List<DatosItemApi> secondaryMuscles;
  private DatosTextosApi instructions;
  private DatosTextosApi mistakes;
  private DatosImagenesApi images;
  private String muscleMap;

  public EjercicioApi() {}

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }

  public DatosItemApi getName() {
    return name;
  }

  public void setName(DatosItemApi name) {
    this.name = name;
  }

  public DatosItemApi getGroup() {
    return group;
  }

  public void setGroup(DatosItemApi group) {
    this.group = group;
  }

  public DatosItemApi getEquipment() {
    return equipment;
  }

  public void setEquipment(DatosItemApi equipment) {
    this.equipment = equipment;
  }

  public List<DatosItemApi> getPrimaryMuscles() {
    return primaryMuscles != null ? primaryMuscles : Collections.emptyList();
  }

  public void setPrimaryMuscles(List<DatosItemApi> primaryMuscles) {
    this.primaryMuscles = primaryMuscles;
  }

  public List<DatosItemApi> getSecondaryMuscles() {
    return secondaryMuscles != null ? secondaryMuscles : Collections.emptyList();
  }

  public void setSecondaryMuscles(List<DatosItemApi> secondaryMuscles) {
    this.secondaryMuscles = secondaryMuscles;
  }

  public DatosTextosApi getInstructions() {
    return instructions;
  }

  public void setInstructions(DatosTextosApi instructions) {
    this.instructions = instructions;
  }

  public DatosTextosApi getMistakes() {
    return mistakes;
  }

  public void setMistakes(DatosTextosApi mistakes) {
    this.mistakes = mistakes;
  }

  public DatosImagenesApi getImages() {
    return images;
  }

  public void setImages(DatosImagenesApi images) {
    this.images = images;
  }

  public String getMuscleMap() {
    return muscleMap;
  }

  public void setMuscleMap(String muscleMap) {
    this.muscleMap = muscleMap;
  }
}
