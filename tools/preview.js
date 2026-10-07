// Vista previa estática de las vistas Thymeleaf (sin Java ni Docker), con recarga automática.
// Uso: node tools/preview.js   ->  http://localhost:4000/bienvenida
// Al guardar un .html, .css o .js de src/main/webapp el navegador se recarga solo.
const http = require("http");
const fs = require("fs");
const path = require("path");

const RAIZ = path.join(__dirname, "..", "src", "main", "webapp");
const VISTAS = path.join(RAIZ, "WEB-INF", "views", "thymeleaf");
const RECURSOS = path.join(RAIZ, "resources", "core");
const PUERTO = 4000;

const TIPOS = {
  ".css": "text/css; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
  ".jpg": "image/jpeg",
  ".jpeg": "image/jpeg",
  ".png": "image/png",
  ".svg": "image/svg+xml",
  ".webp": "image/webp",
};

const RECARGA = `<script>new EventSource("/__recargar").onmessage = () => location.reload();</script>`;

const fragmento = (html, nombre) => {
  const m = html.match(new RegExp(`<(\\w+)[^>]*th:fragment="${nombre}[^"]*"[^>]*>[\\s\\S]*?</\\1>`));
  return m ? m[0] : "";
};

// En horarios los días salen de un th:each: acá se repite el primero para ver la grilla completa
const DIAS = ["Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"];
const repetirDias = (html) =>
  html.replace(/<div class="dia"[^>]*>[\s\S]*?Sin clases<\/p>\s*<\/div>/, (dia) =>
    DIAS.map((nombre, i) => {
      const copia = dia.replace(/>Lunes</, `>${nombre}<`).replace(/>05\/10</, `>${String(i + 5).padStart(2, "0")}/10<`);
      return i < DIAS.length - 1 ? copia.replace(/<p class="dia-vacio"[^>]*>Sin clases<\/p>/, "") : copia;
    }).join("\n")
  );

const limpiar = (html) =>
  html
    .replace(/th:href="@\{([^}(]*)\((\w+)='?([^)']*)'?\)\}"/g, 'href="$1?$2=$3"')
    .replace(/th:href="@\{([^}]*)\}"/g, 'href="$1"')
    .replace(/th:src="@\{([^}]*)\}"/g, 'src="$1"')
    .replace(/th:attr="([\w-]+)=@\{([^}]*)\}"/g, '$1="$2"')
    .replace(/\sth:[\w-]+="[^"]*"/g, "")
    .replace(/\sth:[\w-]+='[^']*'/g, "");

function renderizar(nombre) {
  const archivo = path.join(VISTAS, nombre + ".html");
  if (!/^[\w-]+$/.test(nombre) || !fs.existsSync(archivo)) return null;
  let html = fs.readFileSync(archivo, "utf8");
  const frag = fs.readFileSync(path.join(VISTAS, "fragmentos.html"), "utf8");

  html = html.replace(/<head th:replace="~\{fragmentos :: cabecera\('([^']*)'\)\}"><\/head>/, (_, t) =>
    fragmento(frag, "cabecera").replace(/th:text="[^"]*">[^<]*<\/title>/, `>${t} - Gimnasio UNLAM</title>`)
  );
  // Sin sesión: marca, subtítulo y los enlaces públicos (Horarios, Crear cuenta, Ingresar)
  html = html.replace(
    /<nav th:replace="~\{fragmentos :: barra\('([^']*)'\)\}"><\/nav>/,
    (_, activo) =>
      '<nav class="barra" aria-label="Principal"><a class="navbar-brand" href="/bienvenida">UNLAM</a>' +
      '<span class="barra-sub">Gimnasio</span><ul class="barra-enlaces">' +
      `<li><a href="/horarios"${activo === "horarios" ? ' aria-current="page"' : ""}>Horarios</a></li>` +
      `<li><a href="/nuevo-usuario"${activo === "nuevo-usuario" ? ' aria-current="page"' : ""}>Crear cuenta</a></li>` +
      (activo === "login" ? "" : '<li><a class="barra-ingresar" href="/login">Ingresar</a></li>') +
      "</ul></nav>"
  );
  html = html.replace(/<th:block th:replace="~\{fragmentos :: scripts\}"><\/th:block>/, fragmento(frag, "scripts"));
  html = limpiar(nombre === "horarios" ? repetirDias(html) : html);
  return html.replace("</body>", RECARGA + "</body>");
}

// Navegadores conectados esperando recarga
const clientes = new Set();
let pendiente;
fs.watch(RAIZ, { recursive: true }, () => {
  clearTimeout(pendiente);
  pendiente = setTimeout(() => clientes.forEach((res) => res.write("data: recargar\n\n")), 100);
});

http
  .createServer((req, res) => {
    const url = decodeURIComponent(req.url.split("?")[0]);

    if (url === "/__recargar") {
      res.writeHead(200, { "Content-Type": "text/event-stream", "Cache-Control": "no-cache" });
      res.write("\n");
      clientes.add(res);
      return req.on("close", () => clientes.delete(res));
    }

    // /css/..., /js/..., /img/... salen de resources/core
    const estatico = path.join(RECURSOS, url);
    if (/^\/(css|js|img)\//.test(url) && estatico.startsWith(RECURSOS) && fs.existsSync(estatico)) {
      res.writeHead(200, { "Content-Type": TIPOS[path.extname(estatico)] || "application/octet-stream" });
      return res.end(fs.readFileSync(estatico));
    }

    const html = renderizar(url.replace(/^\//, "") || "bienvenida");
    if (html === null) {
      res.writeHead(404, { "Content-Type": "text/plain; charset=utf-8" });
      return res.end("No existe esa vista");
    }
    res.writeHead(200, { "Content-Type": "text/html; charset=utf-8" });
    res.end(html);
  })
  .listen(PUERTO, () => console.log(`Vista previa en http://localhost:${PUERTO}/bienvenida`));
