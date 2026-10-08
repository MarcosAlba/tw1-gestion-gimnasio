package com.tallerwebi.presentacion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatosItemApi {

  private String id;
  private String es;
  private String en;

  public DatosItemApi() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getEs() {
    return es != null ? es : en;
  }

  public void setEs(String es) {
    this.es = es;
  }

  public String getEn() {
    return en;
  }

  public void setEn(String en) {
    this.en = en;
  }
}
