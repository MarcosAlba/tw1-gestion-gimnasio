package com.tallerwebi.dominio;

import com.tallerwebi.dominio.enums.TipoMembresia;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import com.tallerwebi.dominio.interfaces.RepositorioMembresia;
import com.tallerwebi.dominio.interfaces.RepositorioUsuario;
import com.tallerwebi.dominio.interfaces.ServicioMembresia;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("servicioMembresia")
@Transactional
public class ServicioMembresiaImpl implements ServicioMembresia {

  private RepositorioMembresia repoMembresia;
  private RepositorioUsuario repoUsuario;

  @Autowired
  public ServicioMembresiaImpl(RepositorioMembresia repoMembresia, RepositorioUsuario repoUsuario) {
    this.repoMembresia = repoMembresia;
    this.repoUsuario = repoUsuario;
  }

  @Override
  public void registrar(Long socioId, TipoMembresia tipo) {
    Usuario socio = repoUsuario.buscarPorId(socioId);
    if (socio == null) {
      throw new UsuarioNoEncontrado();
    }
    repoMembresia.guardar(new Membresia(socio, tipo, LocalDate.now()));
  }

  @Override
  public List<Membresia> historial(Long socioId) {
    return repoMembresia.buscarPorSocio(socioId);
  }

  @Override
  public Membresia obtenerVigente(Long socioId) {
    return repoMembresia.buscarVigente(socioId, LocalDate.now());
  }

  @Override
  public Long obtenerDiasRestantes(Long socioId) {
    Membresia vigente = this.obtenerVigente(socioId);
    if (vigente == null) {
      return null;
    }
    return ChronoUnit.DAYS.between(LocalDate.now(), vigente.getFechaVencimiento());
  }
}
