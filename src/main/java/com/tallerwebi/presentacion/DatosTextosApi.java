package com.tallerwebi.presentacion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DatosTextosApi {

  private List<String> es;
  private List<String> en;

  public DatosTextosApi() {}

  public List<String> getEs() {
    return es != null ? es : (en != null ? en : Collections.emptyList());
  }

  public void setEs(List<String> es) {
    this.es = es;
  }

  public List<String> getEn() {
    return en;
  }

  public void setEn(List<String> en) {
    this.en = en;
  }
}
