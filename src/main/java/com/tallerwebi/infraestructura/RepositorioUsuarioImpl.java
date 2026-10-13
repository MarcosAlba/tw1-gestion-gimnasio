package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import java.util.List;
import java.util.Locale;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioUsuario")
public class RepositorioUsuarioImpl implements RepositorioUsuario {

  private SessionFactory sessionFactory;

  @Autowired
  public RepositorioUsuarioImpl(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public Usuario buscarUsuario(String email, String password) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Usuario where email = :email and password = :password", Usuario.class)
      .setParameter("email", email)
      .setParameter("password", password)
      .uniqueResult();
  }

  @Override
  public void guardar(Usuario usuario) {
    sessionFactory.getCurrentSession().persist(usuario);
  }

  @Override
  public Usuario buscar(String email) {
    return sessionFactory
      .getCurrentSession()
      .createQuery("from Usuario where email = :email", Usuario.class)
      .setParameter("email", email)
      .uniqueResult();
  }

  @Override
  public void modificar(Usuario usuario) {
    Usuario existente = sessionFactory
      .getCurrentSession()
      .createQuery("from Usuario where id = :id", Usuario.class)
      .setParameter("id", usuario.getId())
      .uniqueResult();
    if (existente == null) {
      throw new UsuarioNoEncontrado();
    }
    sessionFactory.getCurrentSession().merge(usuario);
  }

  @Override
  public Usuario buscarPorId(Long id) {
    return sessionFactory.getCurrentSession().get(Usuario.class, id);
  }

  @Override
  public List<Usuario> buscarSocios(String criterio) {
    if (criterio == null || criterio.isBlank()) {
      String hql = "FROM Usuario WHERE rol = 'SOCIO' ORDER BY nombre ASC";
      return sessionFactory.getCurrentSession().createQuery(hql, Usuario.class).getResultList();
    }

    String filtro = "%" + criterio.trim().toLowerCase(Locale.ROOT) + "%";
    String hql =
      "FROM Usuario WHERE rol = 'SOCIO' AND (LOWER(nombre) LIKE :filtro OR LOWER(apellido) LIKE :filtro OR LOWER(email) LIKE :filtro) ORDER BY nombre ASC";
    return sessionFactory
      .getCurrentSession()
      .createQuery(hql, Usuario.class)
      .setParameter("filtro", filtro)
      .getResultList();
  }
}
