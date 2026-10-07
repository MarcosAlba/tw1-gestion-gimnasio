package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Clase;
import com.tallerwebi.dominio.interfaces.RepositorioClase;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioClase")
public class RepositorioClaseImpl implements RepositorioClase {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioClaseImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Clase clase) {
    sessionFactory.getCurrentSession().persist(clase);
  }

  @Override
  public void modificar(Clase clase) {
    sessionFactory.getCurrentSession().merge(clase);
  }

  @Override
  public Clase buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Clase.class, id);
  }

  @Override
  public List<Clase> buscarDesde(LocalDateTime desde) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Clase where inicio >= :desde order by inicio", Clase.class)
      .setParameter("desde", desde)
      .getResultList();
  }
}
