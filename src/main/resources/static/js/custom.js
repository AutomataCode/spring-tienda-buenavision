/**
 * Ejecuta una función solo si el elemento existe en el DOM.
 * Evita errores si el selector no está presente.
 */
function initIfExists(selector, callback) {
    document.addEventListener("DOMContentLoaded", function () {
        const element = document.querySelector(selector);
        if (element && typeof callback === 'function') {
            callback(element);
        }
    });
}

/**
 * Inicializa el carrusel de productos relacionados
 */
function initRelatedProductsCarousel() {
    // Verifica que jQuery y slick estén cargados antes de continuar
    if (typeof $ === 'undefined' || !$.fn.slick) {
        console.warn('Slick o jQuery no están disponibles.');
        return;
    }

    $('#carousel-related-product').slick({
        infinite: true,
        arrows: false,
        slidesToShow: 4,
        slidesToScroll: 3,
        dots: true,
        responsive: [
            { breakpoint: 1024, settings: { slidesToShow: 3, slidesToScroll: 3 } },
            { breakpoint: 600,  settings: { slidesToShow: 2, slidesToScroll: 2 } },
            { breakpoint: 480,  settings: { slidesToShow: 1, slidesToScroll: 1 } }
        ]
    });
}

/**
 * Ejemplo de inicialización (puedes añadir más en el futuro)
 */
initIfExists('#carousel-related-product', initRelatedProductsCarousel);
