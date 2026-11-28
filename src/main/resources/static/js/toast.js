(function () {
  "use strict";

  var DEFAULT_DURATION = 2200;
  var container = document.getElementById("toastContainer");

  function getIconId(type) {
    if (type === "success") return "#icon-check";
    if (type === "error") return "#icon-warning";
    return "#icon-bell";
  }

  function dismissToast(toast) {
    if (!toast || toast.dataset.dismissing === "true") return;
    toast.dataset.dismissing = "true";
    toast.classList.remove("is-visible");
    toast.classList.add("is-leaving");
    setTimeout(function () {
      if (toast.parentNode) {
        toast.parentNode.removeChild(toast);
      }
    }, 240);
  }

  function bindToast(toast, duration) {
    var ms = typeof duration === "number" ? duration : DEFAULT_DURATION;
    var timer = null;
    var remaining = ms;
    var startedAt = Date.now();

    function startTimer() {
      if (ms <= 0) return;
      startedAt = Date.now();
      timer = setTimeout(function () {
        dismissToast(toast);
      }, remaining);
    }

    function pauseTimer() {
      if (timer) {
        clearTimeout(timer);
        timer = null;
        remaining = Math.max(0, remaining - (Date.now() - startedAt));
      }
    }

    var closeBtn = toast.querySelector(".toast__close");
    if (closeBtn) {
      closeBtn.addEventListener("click", function () {
        pauseTimer();
        dismissToast(toast);
      });
    }

    toast.addEventListener("mouseenter", pauseTimer);
    toast.addEventListener("mouseleave", startTimer);

    requestAnimationFrame(function () {
      toast.classList.add("is-visible");
    });
    startTimer();
  }

  function createToastElement(message, type) {
    var safeType =
      type === "success" || type === "error" || type === "info" ? type : "info";
    var toast = document.createElement("div");
    toast.className = "toast toast--" + safeType;
    toast.setAttribute("role", safeType === "error" ? "alert" : "status");

    var iconWrap = document.createElement("span");
    iconWrap.className = "toast__icon";
    iconWrap.setAttribute("aria-hidden", "true");
    var svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
    svg.setAttribute("class", "icon icon--sm");
    var use = document.createElementNS("http://www.w3.org/2000/svg", "use");
    use.setAttribute("href", getIconId(safeType));
    svg.appendChild(use);
    iconWrap.appendChild(svg);

    var content = document.createElement("div");
    content.className = "toast__content";
    var msg = document.createElement("p");
    msg.className = "toast__message";
    msg.textContent = message;
    content.appendChild(msg);

    var closeBtn = document.createElement("button");
    closeBtn.type = "button";
    closeBtn.className = "toast__close";
    closeBtn.setAttribute("aria-label", "Đóng thông báo");
    var closeSvg = document.createElementNS(
      "http://www.w3.org/2000/svg",
      "svg",
    );
    closeSvg.setAttribute("class", "icon icon--xs");
    closeSvg.setAttribute("aria-hidden", "true");
    var closeUse = document.createElementNS(
      "http://www.w3.org/2000/svg",
      "use",
    );
    closeUse.setAttribute("href", "#icon-x");
    closeSvg.appendChild(closeUse);
    closeBtn.appendChild(closeSvg);

    toast.appendChild(iconWrap);
    toast.appendChild(content);
    toast.appendChild(closeBtn);
    return toast;
  }

  function show(message, type, duration) {
    if (!container || !message) return;
    var toast = createToastElement(message, type);
    container.appendChild(toast);
    bindToast(toast, duration);
  }

  if (container) {
    container.querySelectorAll(".toast[data-toast]").forEach(function (toast) {
      bindToast(toast, DEFAULT_DURATION);
    });
  }

  window.StudyMateToast = {
    show: show,
    success: function (msg, duration) {
      show(msg, "success", duration);
    },
    error: function (msg, duration) {
      show(msg, "error", duration);
    },
    info: function (msg, duration) {
      show(msg, "info", duration);
    },
  };
})();
