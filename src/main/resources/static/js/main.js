/* ═══════════════════════════════════════════════════════
   main.js  —  Grand Hotel Frontend Core
   ═══════════════════════════════════════════════════════ */

const API_BASE = 'http://localhost:8080/api';

/* ── AUTH HELPERS ──────────────────────────────────────── */
function getToken()        { return localStorage.getItem('token'); }
function getUser()         { try { return JSON.parse(localStorage.getItem('user')); } catch { return null; } }
function isLoggedIn()      { return !!getToken(); }
function isAdmin()         { const u = getUser(); return u && u.role === 'ADMIN'; }
function setSession(data)  {
  localStorage.setItem('token', data.accessToken);
  localStorage.setItem('refreshToken', data.refreshToken);
  localStorage.setItem('user', JSON.stringify({
    id: data.userId, email: data.email,
    fullName: data.fullName, role: data.role
  }));
}
function clearSession()    {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
}

/* ── API FETCH WRAPPER ─────────────────────────────────── */
async function apiFetch(path, opts = {}) {
  const token = getToken();
  const headers = { 'Content-Type': 'application/json', ...(opts.headers || {}) };
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(`${API_BASE}${path}`, {
    ...opts, headers,
    body: opts.body ? JSON.stringify(opts.body) : undefined
  });

  const json = await res.json().catch(() => ({}));

  if (res.status === 401) {
    clearSession();
    window.location.href = resolveRoot('pages/login.html');
    return null;
  }

  // ✅ Error असल्यास exact backend message throw करा
  if (!res.ok) {
    const msg = json?.message || json?.data?.message || json?.error
                || `Server error (${res.status})`;
    const err = new Error(msg);
    err.status = res.status;
    err.data = json;
    throw err;
  }

  return json;
}

/* resolve path to root whether in root or pages/ subfolder */
function resolveRoot(path) {
  return window.location.pathname.includes('/pages/') ? `../${path}` : path;
}

/* ── NAVBAR ────────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
  initNavbar();
  initHamburger();
  updateNavAuth();
  injectToast();
  injectModal();
});

function initNavbar() {
  const navbar = document.getElementById('navbar');
  if (!navbar) return;
  const onScroll = () => {
    if (window.scrollY > 50) navbar.classList.add('scrolled');
    else navbar.classList.remove('scrolled');
  };
  window.addEventListener('scroll', onScroll, { passive: true });
  onScroll();
}

function initHamburger() {
  const btn  = document.getElementById('hamburger');
  const menu = document.getElementById('navLinks');
  if (!btn || !menu) return;
  btn.addEventListener('click', () => {
    menu.classList.toggle('open');
    btn.classList.toggle('open');
  });
}

function updateNavAuth() {
  const actions = document.querySelector('.nav-actions');
  if (!actions) return;
  if (isLoggedIn()) {
    const u = getUser();
    const initials = (u?.fullName || 'U').split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
    actions.innerHTML = `
      <div class="nav-user-menu" id="navUserMenu">
        <div class="nav-avatar" onclick="toggleUserMenu()">${initials}</div>
        <div class="nav-dropdown" id="navDropdown">
          <div class="nav-dropdown-header">
            <strong>${u?.fullName || 'Guest'}</strong>
            <span>${u?.email || ''}</span>
          </div>
          ${isAdmin()
            ? `<a href="${resolveRoot('pages/dashboard-admin.html')}">Admin Dashboard</a>`
            : `<a href="${resolveRoot('pages/dashboard.html')}">My Dashboard</a>`}
          <a href="${resolveRoot('pages/my-bookings.html')}">My Bookings</a>
          <a href="${resolveRoot('pages/profile.html')}">Profile</a>
          <div class="nav-dropdown-divider"></div>
          <a href="#" onclick="logout()">Sign Out</a>
        </div>
      </div>
      <a href="${resolveRoot('pages/rooms.html')}" class="btn-primary">Book Now</a>`;
  }
}

function toggleUserMenu() {
  document.getElementById('navDropdown')?.classList.toggle('open');
}

document.addEventListener('click', e => {
  const menu = document.getElementById('navUserMenu');
  if (menu && !menu.contains(e.target)) {
    document.getElementById('navDropdown')?.classList.remove('open');
  }
});

async function logout() {
  try {
    await apiFetch('/auth/logout', { method: 'POST' });
  } catch (_) {}
  clearSession();
  showToast('Signed out successfully', 'success');
  setTimeout(() => window.location.href = resolveRoot('index.html'), 800);
}

/* ── TOAST ─────────────────────────────────────────────── */
function injectToast() {
  if (document.getElementById('globalToast')) return;
  const el = document.createElement('div');
  el.id = 'globalToast';
  el.className = 'toast';
  el.innerHTML = `<span id="toastIcon"></span><span id="toastMsg"></span>`;
  document.body.appendChild(el);
}

let toastTimer;
function showToast(msg, type = 'success') {
  const el  = document.getElementById('globalToast');
  const ico = document.getElementById('toastIcon');
  const txt = document.getElementById('toastMsg');
  if (!el) return;
  ico.textContent = type === 'success' ? '✓' : type === 'error' ? '✗' : 'ℹ';
  txt.textContent = msg;
  el.className = `toast ${type} show`;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => el.classList.remove('show'), 3500);
}

/* ── MODAL ─────────────────────────────────────────────── */
function injectModal() {
  if (document.getElementById('globalModal')) return;
  const overlay = document.createElement('div');
  overlay.id = 'globalModal';
  overlay.className = 'modal-overlay';
  overlay.innerHTML = `
    <div class="modal" id="modalContent">
      <div class="modal-header">
        <h3 id="modalTitle">Details</h3>
        <button class="modal-close" onclick="closeModal()">✕</button>
      </div>
      <div id="modalBody"></div>
    </div>`;
  overlay.addEventListener('click', e => { if (e.target === overlay) closeModal(); });
  document.body.appendChild(overlay);
}

function openModal(title) {
  if (title) document.getElementById('modalTitle').textContent = title;
  document.getElementById('globalModal')?.classList.add('open');
  document.body.style.overflow = 'hidden';
}
function closeModal() {
  document.getElementById('globalModal')?.classList.remove('open');
  document.body.style.overflow = '';
}

/* ── FORMAT HELPERS ────────────────────────────────────── */
function fmtPrice(n)  { return '₹' + Number(n).toLocaleString('en-IN'); }
function fmtDate(str) {
  if (!str) return '—';
  return new Date(str).toLocaleDateString('en-IN', { day:'2-digit', month:'short', year:'numeric' });
}
function fmtDateTime(str) {
  if (!str) return '—';
  return new Date(str).toLocaleString('en-IN', { day:'2-digit', month:'short', year:'numeric', hour:'2-digit', minute:'2-digit' });
}
function statusBadge(status) {
  const map = {
    PENDING:    ['#f59e0b','#fffbeb'],
    CONFIRMED:  ['#3b82f6','#eff6ff'],
    CHECKED_IN: ['#10b981','#f0fdf4'],
    CHECKED_OUT:['#6b7280','#f9fafb'],
    CANCELLED:  ['#ef4444','#fef2f2'],
    NO_SHOW:    ['#8b5cf6','#f5f3ff'],
    COMPLETED:  ['#10b981','#f0fdf4'],
    FAILED:     ['#ef4444','#fef2f2'],
    REFUNDED:   ['#8b5cf6','#f5f3ff'],
    OPEN:       ['#3b82f6','#eff6ff'],
    RESOLVED:   ['#10b981','#f0fdf4'],
    IN_PROGRESS:['#f59e0b','#fffbeb'],
    CLOSED:     ['#6b7280','#f9fafb'],
    REQUESTED:  ['#3b82f6','#eff6ff'],
  };
  const [color, bg] = map[status] || ['#6b7280','#f9fafb'];
  return `<span style="background:${bg};color:${color};padding:3px 10px;border-radius:100px;font-size:.75rem;font-weight:600">${status}</span>`;
}

/* ── FORM VALIDATION ───────────────────────────────────── */
function showFieldError(inputId, msg) {
  const el = document.getElementById(inputId);
  if (!el) return;
  el.style.borderColor = '#ef4444';
  let err = el.parentNode.querySelector('.field-error');
  if (!err) {
    err = document.createElement('span');
    err.className = 'field-error';
    err.style.cssText = 'color:#ef4444;font-size:.75rem;margin-top:4px;display:block';
    el.parentNode.appendChild(err);
  }
  err.textContent = msg;
}
function clearFieldErrors(formEl) {
  formEl.querySelectorAll('.field-error').forEach(e => e.remove());
  formEl.querySelectorAll('input,select,textarea').forEach(e => e.style.borderColor = '');
}

/* ── LOADING BUTTON ────────────────────────────────────── */
function btnLoading(btn, loading) {
  if (loading) {
    btn.dataset.orig = btn.innerHTML;
    btn.innerHTML = `<span class="loader"></span> Loading...`;
    btn.disabled = true;
  } else {
    btn.innerHTML = btn.dataset.orig || btn.innerHTML;
    btn.disabled = false;
  }
}

/* ── PROTECTED PAGE GUARD ──────────────────────────────── */
function requireAuth(adminOnly = false) {
  if (!isLoggedIn()) {
    window.location.href = resolveRoot('pages/login.html');
    return false;
  }
  if (adminOnly && !isAdmin()) {
    window.location.href = resolveRoot('pages/dashboard.html');
    return false;
  }
  return true;
}

/* ── INJECT NAV DROPDOWN CSS ───────────────────────────── */
(function injectNavStyles() {
  const style = document.createElement('style');
  style.textContent = `
    .nav-avatar{
      width:36px;height:36px;border-radius:50%;
      background:var(--gold,#c9a84c);color:#1a0a00;
      display:flex;align-items:center;justify-content:center;
      font-size:.8rem;font-weight:700;cursor:pointer;
      border:2px solid rgba(201,168,76,.4);transition:.2s;
    }
    .nav-avatar:hover{border-color:var(--gold,#c9a84c)}
    .nav-user-menu{position:relative}
    .nav-dropdown{
      position:absolute;top:calc(100% + 12px);right:0;
      background:#fff;border:1px solid #e5e7eb;
      border-radius:14px;min-width:200px;
      box-shadow:0 12px 40px rgba(0,0,0,.15);
      opacity:0;pointer-events:none;
      transform:translateY(-8px);
      transition:all .2s ease;z-index:200;
      overflow:hidden;
    }
    .nav-dropdown.open{opacity:1;pointer-events:all;transform:translateY(0)}
    .nav-dropdown-header{padding:14px 18px;background:#f9fafb;border-bottom:1px solid #e5e7eb}
    .nav-dropdown-header strong{display:block;font-size:.88rem;color:#111}
    .nav-dropdown-header span{font-size:.76rem;color:#6b7280}
    .nav-dropdown a{
      display:block;padding:11px 18px;font-size:.86rem;
      color:#374151;transition:background .15s;
    }
    .nav-dropdown a:hover{background:#f3f4f6;color:#111}
    .nav-dropdown-divider{height:1px;background:#e5e7eb;margin:4px 0}
  `;
  document.head.appendChild(style);
})();