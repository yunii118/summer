window.addEventListener("DOMContentLoaded", function() {

    /* 상품 썸네일 이벤트 처리 S */
    const thumbs = document.querySelectorAll(".thumbs .thumb");

    const mainImage = document.querySelector(".product_images .main_image img");
    if (mainImage) {
        for (const el of thumbs) {
            el.addEventListener("mouseenter", function() {
                const url = this.dataset.url;
                mainImage.src = url;
            });
        }
    }
    /* 상품 썸네일 이벤트 처리 E */
}