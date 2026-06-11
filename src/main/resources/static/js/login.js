const container = document.getElementById('container');
const registerBtn = document.getElementById('register');
const loginBtn = document.getElementById('login');
const themeToggle = document.getElementById('themeToggle');

// Переключение режимов формы (Вход / Регистрация)
registerBtn.addEventListener('click', (e) => {
    e.preventDefault();
    container.classList.add("active");
});

loginBtn.addEventListener('click', (e) => {
    e.preventDefault();
    container.classList.remove("active");
});

// --- Работа с Cookie и Темой ---
function setCookie(name, value, days) {
    const maxAge = days * 24 * 60 * 60;
    document.cookie = `${name}=${encodeURIComponent(value)}; path=/; max-age=${maxAge}; samesite=lax`;
}

function getCookie(name) {
    const value = '; ' + document.cookie;
    const parts = value.split('; ' + name + '=');
    if (parts.length === 2) return decodeURIComponent(parts.pop().split(';').shift());
    return null;
}

function applyTheme(theme, persist) {
    const root = document.documentElement;
    const isDark = theme === 'dark';
    root.classList.toggle('dark-theme', isDark);
    root.setAttribute('data-theme', isDark ? 'dark' : 'light');
    if (persist !== false) setCookie('smart-idm-theme', isDark ? 'dark' : 'light', 365);
}

// Инициализация темы при загрузке страницы
document.addEventListener('DOMContentLoaded', () => {
    applyTheme(getCookie('smart-idm-theme') || 'light', false);

    if (themeToggle) {
        themeToggle.addEventListener('click', () => {
            const isDarkNow = !document.documentElement.classList.contains('dark-theme');
            applyTheme(isDarkNow ? 'dark' : 'light', true);
        });
    }
});