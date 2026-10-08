// Perfil: editor de la foto (encuadre y zoom) y control de tamaño.
// El servidor vuelve a validar todo; esto solo evita subir archivos que se van a rechazar.
const TIPOS_PERMITIDOS = ["image/jpeg", "image/png", "image/webp"];

// Editor: lienzo cuadrado con un círculo de recorte centrado
const LADO_LIENZO = 320;
const DIAMETRO = 260;
const MARGEN = (LADO_LIENZO - DIAMETRO) / 2;
const LADO_SALIDA = 512;
const ZOOM_MAXIMO = 4;
const PASO_TECLADO = 10;

const entradaFoto = document.getElementById("foto");
const avatar = document.getElementById("avatar");
const mensajeError = document.getElementById("foto-error");

const editor = document.getElementById("editor-foto");
const lienzo = document.getElementById("editor-canvas");
const contexto = lienzo.getContext("2d");
const controlZoom = document.getElementById("editor-zoom");

// Estado del editor: imagen cargada, escala base (la mínima que cubre el círculo) y posición
let imagen = null;
let escalaBase = 1;
let zoom = 1;
let posX = 0;
let posY = 0;
let arrastre = null;

function mostrarError(texto) {
  mensajeError.textContent = texto;
  mensajeError.hidden = texto === "";
}

function mostrarVistaPrevia(archivo) {
  let imagenAvatar = document.getElementById("avatar-imagen");
  if (!imagenAvatar) {
    imagenAvatar = document.createElement("img");
    imagenAvatar.id = "avatar-imagen";
    imagenAvatar.alt = "Vista previa de tu foto";
    avatar.replaceChildren(imagenAvatar);
  }
  imagenAvatar.src = URL.createObjectURL(archivo);
}

function escalaActual() {
  return escalaBase * zoom;
}

// La imagen siempre tiene que cubrir el círculo: no se puede dejar un borde vacío
function limitarPosicion() {
  const ancho = imagen.width * escalaActual();
  const alto = imagen.height * escalaActual();
  posX = Math.min(MARGEN, Math.max(MARGEN + DIAMETRO - ancho, posX));
  posY = Math.min(MARGEN, Math.max(MARGEN + DIAMETRO - alto, posY));
}

function dibujar() {
  contexto.clearRect(0, 0, LADO_LIENZO, LADO_LIENZO);
  contexto.drawImage(imagen, posX, posY, imagen.width * escalaActual(), imagen.height * escalaActual());

  // Oscurece lo que queda fuera del círculo y marca el borde
  const centro = LADO_LIENZO / 2;
  contexto.save();
  contexto.fillStyle = "rgba(0, 0, 0, 0.6)";
  contexto.beginPath();
  contexto.rect(0, 0, LADO_LIENZO, LADO_LIENZO);
  contexto.arc(centro, centro, DIAMETRO / 2, 0, Math.PI * 2, true);
  contexto.fill("evenodd");
  contexto.restore();

  contexto.lineWidth = 3;
  contexto.strokeStyle = "#fff";
  contexto.beginPath();
  contexto.arc(centro, centro, DIAMETRO / 2, 0, Math.PI * 2);
  contexto.stroke();
}

function reiniciarEncuadre() {
  escalaBase = Math.max(DIAMETRO / imagen.width, DIAMETRO / imagen.height);
  zoom = 1;
  controlZoom.value = "1";
  posX = (LADO_LIENZO - imagen.width * escalaBase) / 2;
  posY = (LADO_LIENZO - imagen.height * escalaBase) / 2;
  dibujar();
}

// Cambia el zoom manteniendo fijo el punto de la imagen que está en el centro del círculo
function cambiarZoom(nuevoZoom) {
  const centro = LADO_LIENZO / 2;
  const puntoX = (centro - posX) / escalaActual();
  const puntoY = (centro - posY) / escalaActual();
  zoom = Math.min(ZOOM_MAXIMO, Math.max(1, nuevoZoom));
  controlZoom.value = String(zoom);
  posX = centro - puntoX * escalaActual();
  posY = centro - puntoY * escalaActual();
  limitarPosicion();
  dibujar();
}

function abrirEditor(archivo) {
  const url = URL.createObjectURL(archivo);
  const nueva = new Image();
  nueva.onload = () => {
    URL.revokeObjectURL(url);
    imagen = nueva;
    reiniciarEncuadre();
    editor.showModal();
    lienzo.focus();
  };
  nueva.onerror = () => {
    URL.revokeObjectURL(url);
    entradaFoto.value = "";
    mostrarError("No pudimos leer esa imagen. Probá con otra.");
  };
  nueva.src = url;
}

function cerrarSinAplicar() {
  entradaFoto.value = "";
  imagen = null;
  if (editor.open) {
    editor.close();
  }
}

// Genera el recorte cuadrado y lo deja como el archivo que se va a enviar
function aplicarRecorte() {
  const salida = document.createElement("canvas");
  salida.width = LADO_SALIDA;
  salida.height = LADO_SALIDA;
  const factor = LADO_SALIDA / DIAMETRO;
  salida
    .getContext("2d")
    .drawImage(
      imagen,
      (posX - MARGEN) * factor,
      (posY - MARGEN) * factor,
      imagen.width * escalaActual() * factor,
      imagen.height * escalaActual() * factor
    );

  salida.toBlob(
    (blob) => {
      if (!blob || blob.size > Number(entradaFoto.dataset.maxBytes)) {
        cerrarSinAplicar();
        mostrarError("La foto pesa más de 2 MB. Elegí una más liviana.");
        return;
      }
      const recorte = new File([blob], "perfil.jpg", { type: "image/jpeg" });
      const archivos = new DataTransfer();
      archivos.items.add(recorte);
      entradaFoto.files = archivos.files;
      mostrarVistaPrevia(recorte);
      imagen = null;
      editor.close();
    },
    "image/jpeg",
    0.9
  );
}

entradaFoto.addEventListener("change", () => {
  const archivo = entradaFoto.files[0];
  mostrarError("");
  if (!archivo) {
    return;
  }
  if (!TIPOS_PERMITIDOS.includes(archivo.type)) {
    entradaFoto.value = "";
    mostrarError("El archivo tiene que ser una imagen JPG, PNG o WebP.");
  } else if (archivo.size > Number(entradaFoto.dataset.maxOriginalBytes)) {
    entradaFoto.value = "";
    mostrarError("La imagen es demasiado pesada. Elegí una de menos de 10 MB.");
  } else {
    abrirEditor(archivo);
  }
});

// Arrastrar con el mouse o el dedo
lienzo.addEventListener("pointerdown", (evento) => {
  arrastre = { x: evento.clientX, y: evento.clientY };
  lienzo.setPointerCapture(evento.pointerId);
});

lienzo.addEventListener("pointermove", (evento) => {
  if (!arrastre || !imagen) {
    return;
  }
  const proporcion = LADO_LIENZO / lienzo.clientWidth;
  posX += (evento.clientX - arrastre.x) * proporcion;
  posY += (evento.clientY - arrastre.y) * proporcion;
  arrastre = { x: evento.clientX, y: evento.clientY };
  limitarPosicion();
  dibujar();
});

lienzo.addEventListener("pointerup", () => {
  arrastre = null;
});

lienzo.addEventListener("pointercancel", () => {
  arrastre = null;
});

// Rueda del mouse: zoom
lienzo.addEventListener(
  "wheel",
  (evento) => {
    evento.preventDefault();
    cambiarZoom(zoom + (evento.deltaY < 0 ? 0.1 : -0.1));
  },
  { passive: false }
);

// Teclado: flechas para mover
lienzo.addEventListener("keydown", (evento) => {
  const movimientos = {
    ArrowLeft: [PASO_TECLADO, 0],
    ArrowRight: [-PASO_TECLADO, 0],
    ArrowUp: [0, PASO_TECLADO],
    ArrowDown: [0, -PASO_TECLADO],
  };
  const movimiento = movimientos[evento.key];
  if (movimiento && imagen) {
    evento.preventDefault();
    posX += movimiento[0];
    posY += movimiento[1];
    limitarPosicion();
    dibujar();
  }
});

controlZoom.addEventListener("input", () => cambiarZoom(Number(controlZoom.value)));
document.getElementById("editor-reiniciar").addEventListener("click", reiniciarEncuadre);
document.getElementById("editor-aplicar").addEventListener("click", aplicarRecorte);
document.getElementById("editor-cancelar").addEventListener("click", cerrarSinAplicar);
// Escape también cancela
editor.addEventListener("cancel", () => {
  entradaFoto.value = "";
  imagen = null;
});
