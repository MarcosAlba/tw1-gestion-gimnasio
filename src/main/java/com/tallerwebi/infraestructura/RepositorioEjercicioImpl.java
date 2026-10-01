package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Ejercicio;
import com.tallerwebi.dominio.enums.CapacidadFisica;
import com.tallerwebi.dominio.interfaces.RepositorioEjercicio;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioEjercicio")
public class RepositorioEjercicioImpl implements RepositorioEjercicio {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioEjercicioImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Ejercicio ejercicio) {
    sessionFactory.getCurrentSession().persist(ejercicio);
  }

  @Override
  public Ejercicio buscarPorNombre(String nombre) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Ejercicio where nombre = :nombre", Ejercicio.class)
      .setParameter("nombre", nombre)
      .uniqueResult();
  }

  @Override
  public List<Ejercicio> buscarTodos() {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Ejercicio", Ejercicio.class)
      .getResultList();
  }

  @Override
  public List<Ejercicio> buscarPorCapacidad(CapacidadFisica capacidad) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Ejercicio where capacidad = :capacidad", Ejercicio.class)
      .setParameter("capacidad", capacidad)
      .getResultList();
  }
}
