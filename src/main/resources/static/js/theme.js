(function () {
  "use strict";

  /* ============================================================
     1. CHUYEN DOI CHU DE SANG / TOI / HE THONG
     ============================================================ */
  var STORAGE_KEY = "studymate-theme";
  var mediaQuery = window.matchMedia("(prefers-color-scheme: dark)");

  function resolveTheme(stored) {
    if (stored === "dark") return "dark";
    if (stored === "system") return mediaQuery.matches ? "dark" : "light";
    return "light";
  }

  function applyTheme(stored) {
    var resolved = resolveTheme(stored);
    document.documentElement.setAttribute("data-theme", resolved);
    updateMeta(resolved === "dark");
  }

  function updateMeta(isDark) {
    var color = isDark ? "#0F172A" : "#F8FAFC";
    var meta = document.querySelector('meta[name="theme-color"]');
    if (meta) meta.setAttribute("content", color);
  }

  function saveTheme(value) {
    try {
      localStorage.setItem(STORAGE_KEY, value);
    } catch (e) {}
  }

  function updateButtons(stored) {
    document.querySelectorAll(".theme-switcher__btn").forEach(function (btn) {
      btn.setAttribute(
        "aria-pressed",
        btn.dataset.themeValue === stored ? "true" : "false"
      );
    });
  }

  document.addEventListener("click", function (e) {
    var btn = e.target.closest(".theme-switcher__btn");
    if (!btn) return;
    var val = btn.dataset.themeValue;
    saveTheme(val);
    applyTheme(val);
    updateButtons(val);
  });

  mediaQuery.addEventListener("change", function () {
    var stored;
    try {
      stored = localStorage.getItem(STORAGE_KEY);
    } catch (e) {}
    if (stored === "system") applyTheme("system");
  });

  var current;
  try {
    current = localStorage.getItem(STORAGE_KEY) || "light";
  } catch (e) {
    current = "light";
  }
  updateButtons(current);

  /* ============================================================
     2. HIEN / AN MAT KHAU (PASSWORD TOGGLE)
     ============================================================ */
  document.querySelectorAll(".btn-toggle-password").forEach(function (btn) {
    btn.addEventListener("click", function () {
      var wrapper = btn.closest(".input-wrapper");
      if (!wrapper) return;
      var input = wrapper.querySelector("input");
      if (!input) return;

      var eyeShow = btn.querySelector(".icon-eye-show");
      var eyeHide = btn.querySelector(".icon-eye-hide");

      if (input.type === "password") {
        input.type = "text";
        btn.setAttribute("aria-label", "Ẩn mật khẩu");
        btn.setAttribute("title", "Ẩn mật khẩu");
        btn.setAttribute("aria-pressed", "true");
        if (eyeShow) eyeShow.style.display = "none";
        if (eyeHide) eyeHide.style.display = "inline";
      } else {
        input.type = "password";
        btn.setAttribute("aria-label", "Hiện mật khẩu");
        btn.setAttribute("title", "Hiện mật khẩu");
        btn.setAttribute("aria-pressed", "false");
        if (eyeShow) eyeShow.style.display = "inline";
        if (eyeHide) eyeHide.style.display = "none";
      }
    });
  });

  /* ============================================================
     3. MENU AVATAR NGUOI DUNG
     ============================================================ */
  var userMenuBtn = document.getElementById("userMenuBtn");
  var userMenuDropdown = document.getElementById("userMenuDropdown");
  var userMenuContainer = document.querySelector(".user-menu");

  if (userMenuBtn && userMenuDropdown && userMenuContainer) {
    function openUserMenu() {
      userMenuDropdown.removeAttribute("hidden");
      userMenuBtn.setAttribute("aria-expanded", "true");
    }

    function closeUserMenu() {
      userMenuDropdown.setAttribute("hidden", "");
      userMenuBtn.setAttribute("aria-expanded", "false");
    }

    userMenuBtn.addEventListener("click", function (e) {
      e.stopPropagation();
      if (userMenuDropdown.hasAttribute("hidden")) {
        openUserMenu();
      } else {
        closeUserMenu();
      }
    });

    document.addEventListener("keydown", function (e) {
      if (e.key === "Escape" && !userMenuDropdown.hasAttribute("hidden")) {
        closeUserMenu();
        userMenuBtn.focus();
      }
    });

    document.addEventListener("click", function (e) {
      if (
        !userMenuDropdown.hasAttribute("hidden") &&
        !userMenuContainer.contains(e.target)
      ) {
        closeUserMenu();
      }
    });

    userMenuContainer.addEventListener("focusout", function () {
      if (!userMenuDropdown.hasAttribute("hidden")) {
        setTimeout(function () {
          if (!userMenuContainer.contains(document.activeElement)) {
            closeUserMenu();
          }
        }, 0);
      }
    });
  }
})();