package com.tallerwebi.dominio.interfaces;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.EdadInvalida;
import com.tallerwebi.dominio.excepcion.FotoDemasiadoGrande;
import com.tallerwebi.dominio.excepcion.FotoInvalida;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;

public interface ServicioPerfil {
  Usuario obtener(Long usuarioId) throws UsuarioNoEncontrado;
  void actualizar(Long usuarioId, Usuario cambios, byte[] foto, String tipoFoto)
    throws UsuarioNoEncontrado, FotoDemasiadoGrande, FotoInvalida, EdadInvalida;
}
