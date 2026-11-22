/**
 * Fasor Hub - Main JavaScript
 * Progressive enhancement only - page works without JS
 */

(function() {
  'use strict';

  // ========================================
  // Mobile Menu Toggle
  // ========================================

  function initMobileMenu() {
    var toggleBtn = document.querySelector('.topbar__menu-toggle');
    var mobileNav = document.querySelector('.mobile-nav');

    if (!toggleBtn || !mobileNav) return;

    toggleBtn.addEventListener('click', function() {
      var isOpen = mobileNav.classList.toggle('is-open');
      toggleBtn.setAttribute('aria-expanded', isOpen);
    });

    // Close on escape
    document.addEventListener('keydown', function(e) {
      if (e.key === 'Escape' && mobileNav.classList.contains('is-open')) {
        mobileNav.classList.remove('is-open');
        toggleBtn.setAttribute('aria-expanded', 'false');
        toggleBtn.focus();
      }
    });
  }

  // ========================================
  // Toast Auto-dismiss
  // ========================================

  function initToast() {
    var toastContainer = document.querySelector('.toast-container');
    if (!toastContainer) return;

    var toast = toastContainer.querySelector('.toast');
    if (!toast) return;

    // Auto-dismiss after 5 seconds
    var dismissTimeout = setTimeout(function() {
      dismissToast(toast);
    }, 5000);

    // Close button
    var closeBtn = toast.querySelector('.toast__close');
    if (closeBtn) {
      closeBtn.addEventListener('click', function() {
        clearTimeout(dismissTimeout);
        dismissToast(toast);
      });
    }
  }

  function dismissToast(toast) {
    toast.classList.add('is-leaving');
    toast.addEventListener('animationend', function() {
      toast.remove();
    });
  }

  // ========================================
  // Dialog Management
  // ========================================

  function initDialogs() {
    // Open dialog buttons
    document.querySelectorAll('[data-dialog-open]').forEach(function(btn) {
      btn.addEventListener('click', function() {
        var dialogId = btn.getAttribute('data-dialog-open');
        openDialog(dialogId, btn);
      });
    });

    // Close dialog buttons
    document.querySelectorAll('[data-dialog-close]').forEach(function(btn) {
      btn.addEventListener('click', function() {
        var backdrop = btn.closest('.dialog-backdrop');
        if (backdrop) closeDialog(backdrop);
      });
    });

    // Close on backdrop click
    document.querySelectorAll('.dialog-backdrop').forEach(function(backdrop) {
      backdrop.addEventListener('click', function(e) {
        if (e.target === backdrop) {
          closeDialog(backdrop);
        }
      });
    });

    // Close on escape
    document.addEventListener('keydown', function(e) {
      if (e.key === 'Escape') {
        var openDialog = document.querySelector('.dialog-backdrop.is-open');
        if (openDialog) closeDialog(openDialog);
      }
    });
  }

  var lastFocusedElement = null;

  function openDialog(dialogId, trigger) {
    var backdrop = document.getElementById(dialogId);
    if (!backdrop) return;

    lastFocusedElement = trigger || document.activeElement;
    backdrop.classList.add('is-open');
    backdrop.setAttribute('aria-hidden', 'false');

    // Focus first focusable element in dialog
    var dialog = backdrop.querySelector('.dialog');
    var focusable = dialog.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
    if (focusable.length) {
      focusable[0].focus();
    }

    // Trap focus in dialog
    trapFocus(dialog);
  }

  function closeDialog(backdrop) {
    backdrop.classList.remove('is-open');
    backdrop.setAttribute('aria-hidden', 'true');

    if (lastFocusedElement) {
      lastFocusedElement.focus();
      lastFocusedElement = null;
    }
  }

  function trapFocus(element) {
    var focusable = element.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
    var first = focusable[0];
    var last = focusable[focusable.length - 1];

    element.addEventListener('keydown', function(e) {
      if (e.key !== 'Tab') return;

      if (e.shiftKey) {
        if (document.activeElement === first) {
          last.focus();
          e.preventDefault();
        }
      } else {
        if (document.activeElement === last) {
          first.focus();
          e.preventDefault();
        }
      }
    });
  }

  // ========================================
  // Form Validation Error Clear
  // ========================================

  function initFormValidation() {
    // Clear error when input changes
    document.querySelectorAll('.form-input[aria-invalid="true"]').forEach(function(input) {
      var clearError = function() {
        input.removeAttribute('aria-invalid');
        var errorEl = document.getElementById(input.getAttribute('aria-describedby'));
        if (errorEl) {
          errorEl.textContent = '';
        }
        input.removeEventListener('input', clearError);
        input.removeEventListener('change', clearError);
      };

      input.addEventListener('input', clearError);
      input.addEventListener('change', clearError);
    });
  }

  function initPasswordToggle() {
    document.querySelectorAll('[data-toggle-password]').forEach(function(button) {
      var input = document.getElementById(button.getAttribute('data-toggle-password'));
      var icon = button.querySelector('.material-symbols-outlined');
      if (!input || !icon) {
        return;
      }
      button.addEventListener('click', function() {
        var hidden = input.getAttribute('type') === 'password';
        input.setAttribute('type', hidden ? 'text' : 'password');
        icon.textContent = hidden ? 'visibility_off' : 'visibility';
        button.setAttribute('aria-label', button.getAttribute(hidden ? 'data-label-hide' : 'data-label-show'));
      });
    });
  }

  // ========================================
  // Calendar Month Navigation (placeholder)
  // ========================================

  function initCalendar() {
    // Calendar navigation is typically handled by page reload with query params
    // This is a placeholder for potential JS-enhanced navigation
    var calendar = document.querySelector('.calendar');
    if (!calendar) return;

    // Smooth scroll to selected date
    var selected = calendar.querySelector('.calendar__cell--selected');
    if (selected) {
      selected.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
  }

  // ========================================
  // Selection State Management
  // ========================================

  function initSelection() {
    // Handle row/card selection with aria-pressed buttons
    document.querySelectorAll('[data-select-target]').forEach(function(btn) {
      btn.addEventListener('click', function() {
        var targetId = btn.getAttribute('data-select-target');
        var target = document.getElementById(targetId);

        // Deselect all in group
        var group = btn.getAttribute('data-select-group');
        if (group) {
          document.querySelectorAll('[data-select-group="' + group + '"]').forEach(function(otherBtn) {
            otherBtn.setAttribute('aria-pressed', 'false');
            var otherId = otherBtn.getAttribute('data-select-target');
            var otherTarget = document.getElementById(otherId);
            if (otherTarget) {
              otherTarget.classList.remove('table__row--selected', 'card-list__item--selected');
            }
          });
        }

        // Select this one
        btn.setAttribute('aria-pressed', 'true');
        if (target) {
          target.classList.add('table__row--selected', 'card-list__item--selected');
        }

        // Focus detail card if exists
        var detailCard = document.querySelector('[data-detail-for="' + targetId + '"]');
        if (detailCard) {
          var heading = detailCard.querySelector('h2, h3, [tabindex="-1"]');
          if (heading) {
            heading.focus();
            if (!window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
              heading.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
            }
          }
        }
      });
    });
  }

  // ========================================
  // Initialize All
  // ========================================

  function init() {
    initMobileMenu();
    initToast();
    initDialogs();
    initFormValidation();
    initPasswordToggle();
    initCalendar();
    initSelection();
  }

  // Run on DOM ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

})();
