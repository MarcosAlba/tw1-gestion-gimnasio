// Bienvenida: al elegir una clase de la lista se muestra en la tarjeta destacada
(function () {
  const items = document.querySelectorAll(".clase-item");
  const foto = document.getElementById("clase-foto");
  const nombre = document.getElementById("clase-nombre");
  const descripcion = document.getElementById("clase-descripcion");
  const capacidad = document.getElementById("clase-capacidad");
  const intensidad = document.getElementById("clase-intensidad");
  const horarios = document.getElementById("clase-horarios");
  const rutaHorarios = horarios.getAttribute("href").split("?")[0];

  items.forEach(function (item) {
    item.addEventListener("click", function () {
      items.forEach(function (otro) {
        otro.classList.remove("activa");
        otro.setAttribute("aria-pressed", "false");
      });
      item.classList.add("activa");
      item.setAttribute("aria-pressed", "true");

      foto.src = item.dataset.foto;
      foto.alt = "Clase de " + item.dataset.nombre;
      nombre.textContent = item.dataset.nombre;
      descripcion.textContent = item.dataset.descripcion;
      capacidad.textContent = item.dataset.capacidad;
      intensidad.textContent = item.dataset.intensidad;
      horarios.setAttribute("href", rutaHorarios + "?capacidad=" + item.dataset.filtro);
    });
  });
})();

// Bienvenida: al elegir un deporte cambian la foto, el texto y las barras de cada capacidad
(function () {
  const chips = document.querySelectorAll(".deporte-chip");
  const foto = document.getElementById("deporte-foto");
  const marca = document.getElementById("deporte-marca");
  const nombre = document.getElementById("deporte-nombre");
  const descripcion = document.getElementById("deporte-descripcion");
  const capacidades = ["agilidad", "cardio", "coordinacion", "fuerza"];

  chips.forEach(function (chip) {
    chip.addEventListener("click", function () {
      chips.forEach(function (otro) {
        otro.classList.remove("activo");
        otro.setAttribute("aria-pressed", "false");
      });
      chip.classList.add("activo");
      chip.setAttribute("aria-pressed", "true");

      // Si la foto no existe, el onerror la oculta y queda el nombre de fondo
      foto.hidden = false;
      foto.src = chip.dataset.foto;
      foto.alt = chip.dataset.nombre;
      marca.textContent = chip.dataset.nombre;
      nombre.textContent = chip.dataset.nombre;
      descripcion.textContent = chip.dataset.descripcion;
      capacidades.forEach(function (capacidad) {
        document.getElementById("deporte-" + capacidad).dataset.n = chip.dataset[capacidad];
      });
    });
  });
})();
