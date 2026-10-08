package com.tallerwebi.presentacion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatosImagenesApi {

  private String male;
  private String female;

  public DatosImagenesApi() {}

  public String getMale() {
    return male;
  }

  public void setMale(String male) {
    this.male = male;
  }

  public String getFemale() {
    return female;
  }

  public void setFemale(String female) {
    this.female = female;
  }
}
