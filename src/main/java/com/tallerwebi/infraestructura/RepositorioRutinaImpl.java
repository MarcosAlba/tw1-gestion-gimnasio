package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Rutina;
import com.tallerwebi.dominio.interfaces.RepositorioRutina;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioRutina")
public class RepositorioRutinaImpl implements RepositorioRutina {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioRutinaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Rutina rutina) {
    sessionFactory.getCurrentSession().persist(rutina);
  }

  @Override
  public Rutina buscarUltimaDeSocio(Long socioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from Rutina where socio.id = :socioId order by fechaCreacion desc, id desc",
        Rutina.class
      )
      .setParameter("socioId", socioId)
      .setMaxResults(1)
      .uniqueResult();
  }
}
