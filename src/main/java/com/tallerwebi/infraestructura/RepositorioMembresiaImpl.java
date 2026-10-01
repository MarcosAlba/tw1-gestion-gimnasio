package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Membresia;
import com.tallerwebi.dominio.interfaces.RepositorioMembresia;
import java.time.LocalDate;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioMembresia")
public class RepositorioMembresiaImpl implements RepositorioMembresia {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioMembresiaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Membresia membresia) {
    sessionFactory.getCurrentSession().persist(membresia);
  }

  @Override
  public List<Membresia> buscarPorSocio(Long socioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from Membresia where socio.id = :socioId order by fechaInicio desc",
        Membresia.class
      )
      .setParameter("socioId", socioId)
      .getResultList();
  }

  @Override
  public Membresia buscarVigente(Long socioId, LocalDate fecha) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from Membresia where socio.id = :socioId and fechaInicio <= :fecha and fechaVencimiento >= :fecha order by fechaVencimiento desc",
        Membresia.class
      )
      .setParameter("socioId", socioId)
      .setParameter("fecha", fecha)
      .setMaxResults(1)
      .uniqueResult();
  }
}
