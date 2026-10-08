package com.tallerwebi;

import com.tallerwebi.config.DatabaseInitializationConfig;
import com.tallerwebi.config.HibernateConfig;
import com.tallerwebi.config.SpringWebConfig;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class MyServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

  // services and data sources
  @Override
  protected Class<?>[] getRootConfigClasses() {
    return new Class[0];
  }

  // controller, view resolver, handler mapping
  @Override
  protected Class<?>[] getServletConfigClasses() {
    return new Class[] {
      SpringWebConfig.class,
      HibernateConfig.class,
      DatabaseInitializationConfig.class,
    };
  }

  @Override
  protected String[] getServletMappings() {
    return new String[] { "/" };
  }

  // Habilita subir archivos (foto de perfil). Un poco más que los 2 MB que valida el servicio
  @Override
  protected void customizeRegistration(ServletRegistration.Dynamic registration) {
    registration.setMultipartConfig(new MultipartConfigElement("", 5_000_000L, 6_000_000L, 0));
  }
}
