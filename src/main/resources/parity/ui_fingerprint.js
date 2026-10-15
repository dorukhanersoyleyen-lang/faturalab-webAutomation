// -*- coding: utf-8 -*-
// UI Parmak İzi — eski ↔ yeni tasarım karşılaştırması için ekranın fonksiyonel envanteri.
// Kaynak: v2_QA/scripts/utils/ui_fingerprint.js (iki kopya senkron tutulmalı).
// Kullanım: com.faturalab.automation.utils.parity.UiFingerprint bu dosyayı classpath'ten yükleyip
// JavascriptExecutor ile çalıştırır; sonuç ParityAssert ile baseline'a karşı karşılaştırılır.
// Shadow DOM içine de iner (Vaadin / Dasein web component'leri).
function uiFingerprint(rootSel) {
  const fold = s => (s || '').replace(/\s+/g, ' ').trim();
  const visible = el => {
    const r = el.getBoundingClientRect();
    const cs = getComputedStyle(el);
    return r.width > 1 && r.height > 1 && cs.visibility !== 'hidden' && cs.display !== 'none';
  };
  const all = [];
  const walk = node => {
    for (const el of node.querySelectorAll('*')) {
      all.push(el);
      if (el.shadowRoot) walk(el.shadowRoot);
    }
  };
  walk((rootSel && document.querySelector(rootSel)) || document);

  const fieldTags = /^(vaadin-(text|password|email|integer|number|big-decimal)-field|vaadin-text-area|vaadin-combo-box|vaadin-select|vaadin-date-picker|input|textarea|select)$/i;
  const fields = [];
  const seen = new Set();
  for (const el of all) {
    const tag = el.tagName.toLowerCase();
    if (!fieldTags.test(tag)) continue;
    // vaadin-*-field host'unun içindeki ham <input>'u ikinci kez sayma
    if (tag === 'input' && el.closest('vaadin-text-field,vaadin-password-field,vaadin-email-field,vaadin-integer-field,vaadin-number-field,vaadin-combo-box,vaadin-date-picker,vaadin-big-decimal-field')) continue;
    if (tag === 'input' && ['checkbox', 'radio', 'hidden', 'file'].includes(el.type)) continue;
    if (tag === 'textarea' && el.closest('vaadin-text-area')) continue;
    if (!visible(el)) continue;
    const lbl = el.querySelector && el.querySelector('label');
    // Dasein: etiket host'ta değil, sarmalayıcı div.dsn-field içindeki .dsn-form-field-label__main'de
    const dsnLbl = el.closest && el.closest('div.dsn-field') && el.closest('div.dsn-field').querySelector('.dsn-form-field-label__main');
    const label = fold(el.getAttribute('label') || el.label || (dsnLbl ? dsnLbl.textContent : '') || (lbl ? lbl.textContent : '') || el.getAttribute('aria-label'));
    const inner = el.querySelector && el.querySelector('input,textarea');
    const ph = fold(el.getAttribute('placeholder') || el.placeholder);
    const key = tag + '|' + label + '|' + ph;
    if (seen.has(key)) continue; seen.add(key);
    fields.push({
      tag, label, placeholder: ph,
      // host type'ı Dasein'de 'text' olabilir; gerçek tip iç input'tan okunur
      type: (inner && inner.type) || el.getAttribute('type') || el.type || '',
      required: !!(el.required || el.hasAttribute('required') || el.getAttribute('aria-required') === 'true'
        || (inner && (inner.required || inner.getAttribute('aria-required') === 'true'))),
      maxlength: el.getAttribute('maxlength') || el.maxlength || (inner && inner.getAttribute('maxlength')) || null,
      pattern: el.getAttribute('pattern') || el.pattern || null,
      invalid: !!(el.invalid || el.hasAttribute('invalid')),
      error: fold(el.errorMessage || el.getAttribute('error-message')),
      disabled: !!(el.disabled || el.hasAttribute('disabled')),
    });
  }
  // Tıklanabilir ama button/link olmayan öğeler (cursor:pointer span/div) — klavye erişimi kaybını yakalar
  const pseudoClickables = [...new Set(all.filter(e => /^(span|div|label)$/i.test(e.tagName) && e.children.length === 0 && visible(e)
    && getComputedStyle(e).cursor === 'pointer' && !e.closest('vaadin-button,button,a,vaadin-checkbox')
    && fold(e.textContent).length > 0 && fold(e.textContent).length < 60)
    // Tab ile odaklanabilir sayılmak için tabindex >= 0 gerekir (role tek başına odak vermez; tabindex=-1 odaklanmaz)
    .map(e => fold(e.textContent) + (parseInt(e.getAttribute('tabindex'), 10) >= 0 ? '' : ' [klavye-erisilemez]')))];
  const buttons = [...new Set(all.filter(e => (/^(vaadin-button|button)$/i.test(e.tagName) || e.getAttribute('role') === 'button') && visible(e))
    .map(e => fold(e.textContent) || fold(e.getAttribute('aria-label')) || '[ikon:' + ((e.querySelector && e.querySelector('vaadin-icon,iron-icon')?.getAttribute('icon')) || '?') + ']'))];
  const links = [...new Set(all.filter(e => /^(a|vaadin-router-link)$/i.test(e.tagName) && visible(e))
    .map(e => fold(e.textContent) + ' -> ' + (e.getAttribute('href') || '')))];
  const checkboxes = all.filter(e => (/^vaadin-checkbox$/i.test(e.tagName) || (e.tagName === 'INPUT' && e.type === 'checkbox' && !e.closest('vaadin-checkbox'))) && visible(e))
    .map(e => ({ label: fold(e.label || e.textContent), checked: !!e.checked, required: !!(e.required || e.hasAttribute('required')) }));
  const headings = [...new Set(all.filter(e => /^h[1-6]$/i.test(e.tagName) && visible(e)).map(e => fold(e.textContent)).filter(Boolean))];
  const images = [...new Set(all.filter(e => e.tagName === 'IMG' && visible(e))
    .map(e => (e.getAttribute('src') || '').split('?')[0].split('/').slice(-1)[0] + (e.alt ? ' (alt=' + e.alt + ')' : '')))];
  const notifications = [...document.querySelectorAll('vaadin-notification-card')].filter(visible).map(e => fold(e.textContent));
  const dialogs = [...document.querySelectorAll('vaadin-dialog-overlay,[role=dialog]')].filter(visible).map(e => fold(e.textContent).slice(0, 300));
  // Komponent envanteri — tasarım sistemi geçişini kanıtlar (vaadin-* → Dasein/özel elementler)
  const components = {};
  for (const e of all) { const t = e.tagName.toLowerCase(); if (t.includes('-') && visible(e)) components[t] = (components[t] || 0) + 1; }
  // Dasein bileşenleri genelde CSS class ile işaretlenir — class önekleri envanteri
  const classes = {};
  for (const e of all) {
    if (!visible(e) || typeof e.className !== 'string') continue;
    for (const c of e.className.split(/\s+/)) { if (/^(dsn|ds-|dasein|fl-|popup|modal|typography|password-strength)/i.test(c)) classes[c] = (classes[c] || 0) + 1; }
  }
  return {
    url: location.href, title: document.title, lang: document.documentElement.lang,
    viewport: innerWidth + 'x' + innerHeight,
    fields, buttons, pseudoClickables, links, checkboxes, headings, images, notifications, dialogs,
    components, classes, text: fold(document.body.innerText).slice(0, 4000),
  };
}
