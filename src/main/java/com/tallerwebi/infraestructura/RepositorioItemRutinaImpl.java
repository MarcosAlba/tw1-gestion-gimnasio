package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.ItemRutina;
import com.tallerwebi.dominio.interfaces.RepositorioItemRutina;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioItemRutinaImpl implements RepositorioItemRutina {

  private final SessionFactory sessionFactory;

  @Autowired
  public RepositorioItemRutinaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(ItemRutina item) {
    sessionFactory.getCurrentSession().persist(item);
  }

  @Override
  public List<ItemRutina> buscarPorSocio(Long idSocio) {
    String hql = "FROM ItemRutina WHERE socio.id = :idSocio ORDER BY dia ASC, id ASC";
    return sessionFactory
      .getCurrentSession()
      .createQuery(hql, ItemRutina.class)
      .setParameter("idSocio", idSocio)
      .getResultList();
  }

  @Override
  public void eliminar(Long idItem) {
    ItemRutina item = buscarPorId(idItem);
    if (item != null) {
      sessionFactory.getCurrentSession().remove(item);
    }
  }

  @Override
  public ItemRutina buscarPorId(Long idItem) {
    return sessionFactory.getCurrentSession().get(ItemRutina.class, idItem);
  }
}
