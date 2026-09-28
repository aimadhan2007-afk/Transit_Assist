// TransitAssist JS - Modern UI, Theme, Interactivity & Accessibility Utilities

// 1. Initialize theme immediately before render to avoid flash
(function () {
  const savedTheme = localStorage.getItem('transitassist-theme') || 
    (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
  document.documentElement.setAttribute('data-theme', savedTheme);
})();

document.addEventListener('DOMContentLoaded', () => {
  // Theme toggle button setup
  const themeToggles = document.querySelectorAll('.theme-toggle-btn, .theme-btn');
  themeToggles.forEach(btn => {
    btn.addEventListener('click', () => {
      const activeTheme = document.documentElement.getAttribute('data-theme') || 'light';
      const newTheme = activeTheme === 'dark' ? 'light' : 'dark';
      document.documentElement.setAttribute('data-theme', newTheme);
      localStorage.setItem('transitassist-theme', newTheme);
    });
  });

  // Auto-dismiss alerts after 5 seconds with smooth animation
  const alerts = document.querySelectorAll('.alert');
  alerts.forEach(alert => {
    setTimeout(() => {
      alert.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
      alert.style.opacity = '0';
      alert.style.transform = 'translateY(-6px)';
      setTimeout(() => alert.remove(), 400);
    }, 6000);
  });

  // Setup interactive assistance type cards if present on page
  setupAssistanceTypeCards();

  // Close mobile nav when clicking outside
  document.addEventListener('click', (e) => {
    const mobileToggle = document.querySelector('.mobile-toggle');
    const activeNav = document.getElementById('primaryNavLinks') || document.getElementById('publicNavLinks');
    if (activeNav && activeNav.classList.contains('active')) {
      if (!activeNav.contains(e.target) && !mobileToggle.contains(e.target)) {
        activeNav.classList.remove('active');
      }
    }
  });

  // Setup keyboard ESC to close modals
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      const activeModal = document.querySelector('.modal-overlay.active');
      if (activeModal) {
        closeModal(activeModal.id);
      }
    }
  });
});

// Toggle Mobile Nav Menu
function toggleMobileMenu() {
  const activeNav = document.getElementById('primaryNavLinks') || document.getElementById('publicNavLinks');
  if (activeNav) {
    activeNav.classList.toggle('active');
  }
}

// Interactive Assistance Type Selection Cards
function setupAssistanceTypeCards() {
  const selectElem = document.getElementById('assistanceType');
  const cards = document.querySelectorAll('.assistance-type-card');
  if (!selectElem || cards.length === 0) return;

  // Sync initial state if select has value
  if (selectElem.value) {
    cards.forEach(card => {
      if (card.dataset.value === selectElem.value) {
        card.classList.add('active');
      }
    });
  }

  cards.forEach(card => {
    card.addEventListener('click', () => {
      cards.forEach(c => c.classList.remove('active'));
      card.classList.add('active');
      selectElem.value = card.dataset.value;

      // Trigger change event for any listeners
      selectElem.dispatchEvent(new Event('change'));
    });
  });

  // If select dropdown changes manually, sync cards
  selectElem.addEventListener('change', () => {
    cards.forEach(card => {
      card.classList.toggle('active', card.dataset.value === selectElem.value);
    });
  });
}

// Quick fill time helper
function setQuickTime(offsetMinutes) {
  const timeInput = document.getElementById('travelTime');
  if (!timeInput) return;
  
  const now = new Date();
  now.setMinutes(now.getMinutes() + offsetMinutes);
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(Math.floor(now.getMinutes() / 5) * 5).padStart(2, '0');
  timeInput.value = `${hours}:${minutes}`;
}

// Quick fill credentials for demo personas
function fillCredentials(email, password) {
  const emailInput = document.getElementById('email');
  const passwordInput = document.getElementById('password');
  if (emailInput && passwordInput) {
    emailInput.value = email;
    passwordInput.value = password;
    
    // Add visual feedback pulse
    emailInput.style.borderColor = 'var(--primary)';
    passwordInput.style.borderColor = 'var(--primary)';
    setTimeout(() => {
      emailInput.style.borderColor = '';
      passwordInput.style.borderColor = '';
    }, 800);
  }
}

// Modal handling
function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
  }
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.remove('active');
    document.body.style.overflow = '';
  }
}

// Close modal when clicking outside modal card
window.addEventListener('click', (e) => {
  if (e.target.classList.contains('modal-overlay')) {
    e.target.classList.remove('active');
    document.body.style.overflow = '';
  }
});
