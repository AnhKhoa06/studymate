(function () {
  "use strict";
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
    var color = isDark ? "#0E1B20" : "#EEF6F7";
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

  document.querySelectorAll(".theme-switcher__btn").forEach(function (btn) {
    btn.addEventListener("click", function () {
      var val = btn.dataset.themeValue;
      saveTheme(val);
      applyTheme(val);
      updateButtons(val);
    });
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
})();
