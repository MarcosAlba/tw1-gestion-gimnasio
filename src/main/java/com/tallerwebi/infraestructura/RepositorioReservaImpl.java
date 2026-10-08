package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Reserva;
import com.tallerwebi.dominio.enums.EstadoReserva;
import com.tallerwebi.dominio.interfaces.RepositorioReserva;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioReserva")
public class RepositorioReservaImpl implements RepositorioReserva {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioReservaImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void guardar(Reserva reserva) {
    sessionFactory.getCurrentSession().persist(reserva);
  }

  @Override
  public void modificar(Reserva reserva) {
    sessionFactory.getCurrentSession().merge(reserva);
  }

  @Override
  public List<Reserva> buscarPorSocio(Long socioId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from Reserva where socio.id = :socioId order by fechaReserva desc",
        Reserva.class
      )
      .setParameter("socioId", socioId)
      .getResultList();
  }

  @Override
  public int contarConfirmadas(Long claseId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(r) from Reserva r where r.clase.id = :claseId and r.estado = :estado",
        Long.class
      )
      .setParameter("claseId", claseId)
      .setParameter("estado", EstadoReserva.CONFIRMADA)
      .uniqueResult()
      .intValue();
  }

  @Override
  public boolean existeActiva(Long socioId, Long claseId) {
    Long cantidad = sessionFactory
      .getCurrentSession()
      .createQuery(
        "select count(r) from Reserva r where r.socio.id = :socioId and r.clase.id = :claseId and r.estado <> :cancelada",
        Long.class
      )
      .setParameter("socioId", socioId)
      .setParameter("claseId", claseId)
      .setParameter("cancelada", EstadoReserva.CANCELADA)
      .uniqueResult();
    return cantidad > 0;
  }

  @Override
  public Reserva buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Reserva.class, id);
  }

  @Override
  public Reserva buscarPrimeraEnEspera(Long claseId) {
    return sessionFactory
      .getCurrentSession()
      .createQuery(
        "from Reserva where clase.id = :claseId and estado = :enEspera order by fechaReserva asc, id asc",
        Reserva.class
      )
      .setParameter("claseId", claseId)
      .setParameter("enEspera", EstadoReserva.EN_ESPERA)
      .setMaxResults(1)
      .uniqueResult();
  }
}
