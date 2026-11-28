(function () {
  "use strict";

  var toggleBtn = document.getElementById("sidebarToggleBtn");
  var sidebar = document.getElementById("staffSidebar");
  var backdrop = document.getElementById("sidebarBackdrop");

  if (!toggleBtn || !sidebar) return;

  function openDrawer() {
    sidebar.classList.add("is-open");
    toggleBtn.setAttribute("aria-expanded", "true");
    if (backdrop) {
      backdrop.removeAttribute("hidden");
      backdrop.classList.add("is-open");
    }
    document.body.style.overflow = "hidden";
  }

  function closeDrawer() {
    sidebar.classList.remove("is-open");
    toggleBtn.setAttribute("aria-expanded", "false");
    if (backdrop) {
      backdrop.setAttribute("hidden", "");
      backdrop.classList.remove("is-open");
    }
    document.body.style.overflow = "";
    toggleBtn.focus();
  }

  toggleBtn.addEventListener("click", function (e) {
    e.stopPropagation();
    var isOpen = sidebar.classList.contains("is-open");
    if (isOpen) {
      closeDrawer();
    } else {
      openDrawer();
    }
  });

  if (backdrop) {
    backdrop.addEventListener("click", closeDrawer);
  }

  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape" && sidebar.classList.contains("is-open")) {
      closeDrawer();
    }
  });

  document.addEventListener("click", function (e) {
    if (
      sidebar.classList.contains("is-open") &&
      !sidebar.contains(e.target) &&
      !toggleBtn.contains(e.target)
    ) {
      closeDrawer();
    }
  });
})();
