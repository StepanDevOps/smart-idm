// Функции для работы с Cookie
function setCookie(name, value, days) {
    var maxAge = days * 24 * 60 * 60;
    document.cookie = name + '=' + encodeURIComponent(value) + '; path=/; max-age=' + maxAge + '; samesite=lax';
}

function getCookie(name) {
    var value = '; ' + document.cookie;
    var parts = value.split('; ' + name + '=');
    if (parts.length === 2) return decodeURIComponent(parts.pop().split(';').shift());
    return null;
}

// Общая логика применения темы (чтобы не дублировать в макетах)
function applyTheme(theme, persist) {
    var root = document.documentElement;
    var body = document.body;
    var isDark = theme === 'dark';
    root.classList.toggle('dark-theme', isDark);
    root.setAttribute('data-theme', isDark ? 'dark' : 'light');
    if (body) {
        body.classList.toggle('dark-theme', isDark);
    }
    if (persist !== false) {
        setCookie('smart-idm-theme', isDark ? 'dark' : 'light', 365);
    }
}