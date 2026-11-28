(function () {
  "use strict";

  var rail = document.querySelector(".navbar__rail");
  if (!rail) return;

  rail.classList.add("navbar__rail--js");
  var indicator = rail.querySelector(".navbar__indicator");
  var links = Array.from(rail.querySelectorAll(".navbar__link"));
  var activeLink = links.find(function (link) {
    return link.getAttribute("aria-current") === "page";
  });

  var prefersReducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;

  function setLit(target) {
    links.forEach(function (link) {
      if (link === target) {
        link.classList.add("is-lit");
      } else {
        link.classList.remove("is-lit");
      }
    });
  }

  function place(link, animate) {
    if (!link || !indicator) return;

    var width = link.offsetWidth;
    var left = link.offsetLeft;

    if (!animate || prefersReducedMotion) {
      indicator.style.transition = "none";
      indicator.style.width = width + "px";
      indicator.style.transform = "translateX(" + left + "px)";
      // Ép reflow
      void indicator.offsetHeight;
      indicator.style.transition = "";
    } else {
      indicator.style.width = width + "px";
      indicator.style.transform = "translateX(" + left + "px)";
    }
  }

  var STORAGE_KEY = "studymate-nav-prev";

  // Khởi tạo
  if (activeLink) {
    indicator.style.opacity = "1";
    setLit(activeLink);

    var prevPos = null;
    try {
      var raw = sessionStorage.getItem(STORAGE_KEY);
      if (raw) {
        prevPos = JSON.parse(raw);
      }
    } catch (e) {}

    if (prevPos && typeof prevPos.left === "number" && typeof prevPos.width === "number") {
      indicator.style.transition = "none";
      indicator.style.width = prevPos.width + "px";
      indicator.style.transform = "translateX(" + prevPos.left + "px)";
      void indicator.offsetHeight;
      indicator.style.transition = "";

      requestAnimationFrame(function () {
        place(activeLink, true);
      });
    } else {
      place(activeLink, false);
    }
  } else {
    if (indicator) {
      indicator.style.opacity = "0";
    }
    setLit(null);
  }

  // Tương tác hover và focus
  links.forEach(function (link) {
    function onEnter() {
      if (indicator) indicator.style.opacity = "1";
      place(link, true);
      setLit(link);
    }

    link.addEventListener("mouseenter", onEnter);
    link.addEventListener("focus", onEnter);
  });

  function onLeave() {
    if (activeLink) {
      if (indicator) indicator.style.opacity = "1";
      place(activeLink, true);
      setLit(activeLink);
    } else {
      if (indicator) indicator.style.opacity = "0";
      setLit(null);
    }
  }

  rail.addEventListener("mouseleave", onLeave);

  rail.addEventListener("focusout", function (e) {
    // Nếu focus ra ngoài rail
    if (!rail.contains(e.relatedTarget)) {
      onLeave();
    }
  });

  // Lưu vị trí khi chuyển trang
  window.addEventListener("pagehide", function () {
    if (indicator && indicator.style.opacity !== "0") {
      try {
        var currentLink = links.find(function (l) {
          return l.classList.contains("is-lit");
        }) || activeLink;
        if (currentLink) {
          sessionStorage.setItem(
            STORAGE_KEY,
            JSON.stringify({
              left: currentLink.offsetLeft,
              width: currentLink.offsetWidth
            })
          );
        }
      } catch (e) {}
    }
  });

  // Resize có debounce
  var resizeTimeout;
  window.addEventListener("resize", function () {
    clearTimeout(resizeTimeout);
    resizeTimeout = setTimeout(function () {
      if (activeLink) {
        place(activeLink, false);
      }
    }, 100);
  });
})();
