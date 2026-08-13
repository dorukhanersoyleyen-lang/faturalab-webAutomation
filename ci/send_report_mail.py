# -*- coding: utf-8 -*-
"""
FaturaLab QA Otomasyon - test raporu mail gonderici (Jenkins pipeline'dan cagrilir).
Cucumber JSON'dan ozet cikarir, Gunluk Prod Hata Raporu ile AYNI gorsel dilde
(600px kart, CID inline FaturaLab logosu, NAVY baslik) HTML mail olusturur.

Env degiskenleri (Jenkins saglar):
  JOB_NAME, BUILD_NUMBER, BUILD_URL, BUILD_RESULT (SUCCESS/UNSTABLE/FAILURE)
  REPORT_URL   - dahili rapor linki (http://192.168.97.33:8090/latest/); yoksa BUILD_URL
  MAIL_TO      - virgullu alici listesi (varsayilan: 3 kisi)
Calisma dizini repo koku (target/cucumber-reports/*.json okunur).
"""
import os, sys, glob, json, base64, smtplib
from datetime import datetime
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from email.mime.image import MIMEImage
from email.utils import formatdate, make_msgid
sys.stdout.reconfigure(encoding='utf-8', errors='replace')

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
SMTP_HOST = os.environ.get('SMTP_HOST', 'ileti.faturalab.com')
SMTP_PORT = int(os.environ.get('SMTP_PORT', '25'))
MAIL_FROM = os.environ.get('MAIL_FROM', 'automationReport-noreply@ileti.faturalab.com')
# Alici politikasi (kullanici tercihi 03.08.2026, guncelleme 13.08.2026): TO = Dorukhan,
# CC = Huseyin + Enes Erdogan (Scrum Master).
MAIL_TO = [x.strip() for x in os.environ.get(
    'MAIL_TO', 'dorukhan.ersoyleyen@faturalab.com'
).split(',') if x.strip()]
MAIL_CC = [x.strip() for x in os.environ.get(
    'MAIL_CC', 'huseyin.taskin@faturalab.com,enes.erdogan@faturalab0.onmicrosoft.com'
).split(',') if x.strip()]

JOB = os.environ.get('JOB_NAME', 'faturalab-webAutomation-pipeline')
BUILD = os.environ.get('BUILD_NUMBER', '?')
BUILD_URL = os.environ.get('BUILD_URL', '')
RESULT = os.environ.get('BUILD_RESULT', 'UNKNOWN').upper()
REPORT_URL = os.environ.get('REPORT_URL', '') or BUILD_URL


def esc(s):
    return (str(s).replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;'))


def load_logo_b64():
    try:
        with open(os.path.join(SCRIPT_DIR, 'faturalab_logo.b64')) as f:
            return f.read().strip()
    except Exception:
        return None


def parse_cucumber():
    """target/cucumber-reports/*.json -> (toplam, gecen, kalan, feature_ozet[])"""
    total = passed = failed = 0
    features = []
    for path in glob.glob('target/cucumber-reports/*.json'):
        try:
            data = json.load(open(path, encoding='utf-8'))
        except Exception:
            continue
        for feat in data:
            fname = feat.get('name', feat.get('uri', '?'))
            fp = ff = 0
            for el in feat.get('elements', []):
                if el.get('type') != 'scenario':
                    continue
                steps = el.get('steps', []) + el.get('before', []) + el.get('after', [])
                ok = all(s.get('result', {}).get('status') in ('passed', 'skipped', None)
                         for s in steps) and any(
                         s.get('result', {}).get('status') == 'passed' for s in el.get('steps', []))
                total += 1
                if ok:
                    passed += 1; fp += 1
                else:
                    failed += 1; ff += 1
            if fp + ff > 0:
                features.append((fname, fp, ff))
    return total, passed, failed, features


def build_html(total, passed, failed, features, logo_ref):
    # Gunluk Prod Hata Raporu ile ayni gorsel dil (renk sabitleri birebir)
    NAVY = "#14489F"; CYAN = "#1487c7"; INK = "#1f2733"; MUT = "#6b7684"
    LINE = "#dfe3e8"; HEADBG = "#f4f6f8"; CRIT = "#b23b3b"; BG = "#eceff2"; OK = "#1e7d43"

    rate = f'{(passed / total * 100):.0f}%' if total else '—'
    res_txt = {'SUCCESS': 'BAŞARILI', 'UNSTABLE': 'KARARSIZ', 'FAILURE': 'BAŞARISIZ'}.get(RESULT, RESULT)
    res_col = {'SUCCESS': OK, 'UNSTABLE': '#b26a00', 'FAILURE': CRIT}.get(RESULT, MUT)
    gen_dt = datetime.now().strftime('%d.%m.%Y %H:%M')

    def frow(name, fp, ff, last):
        bb = "" if last else f"border-bottom:1px solid {LINE};"
        accent = CRIT if ff else OK
        dot = (f'<span style="display:inline-block;width:6px;height:6px;border-radius:50%;'
               f'background:{accent};margin-right:7px;vertical-align:middle"></span>')
        ktag = (f'<span style="display:inline-block;margin-left:7px;padding:1px 6px;background:#f7e7e6;'
                f'color:{CRIT};font:700 8px \'Segoe UI\',Arial;letter-spacing:.5px;border-radius:9px;'
                f'vertical-align:middle">{ff} KALDI</span>' if ff else '')
        return f"""<tr>
<td style="padding:11px 12px 11px 0;{bb}vertical-align:top">
  <div style="font:600 12px 'Segoe UI',Arial;color:{INK}">{dot}{esc(name)}{ktag}</div></td>
<td align="right" style="padding:11px 12px;{bb}vertical-align:top;white-space:nowrap">
  <span style="font:800 14px 'Segoe UI',Arial;color:{OK}">{fp}</span>
  <span style="font:9px 'Segoe UI',Arial;color:{MUT}"> geçti</span></td>
<td align="right" style="padding:11px 0;{bb}vertical-align:top;white-space:nowrap">
  <span style="font:800 14px 'Segoe UI',Arial;color:{CRIT if ff else MUT}">{ff}</span>
  <span style="font:9px 'Segoe UI',Arial;color:{MUT}"> kaldı</span></td></tr>"""

    items = sorted(features, key=lambda x: -x[2])
    head = f"""<tr>
<th align="left" style="padding:7px 12px 7px 0;border-bottom:2px solid {NAVY};font:700 9px 'Segoe UI',Arial;letter-spacing:.8px;color:{MUT};text-transform:uppercase">Feature</th>
<th align="right" style="padding:7px 12px;border-bottom:2px solid {NAVY};font:700 9px 'Segoe UI',Arial;letter-spacing:.8px;color:{MUT};text-transform:uppercase">Geçti</th>
<th align="right" style="padding:7px 0;border-bottom:2px solid {NAVY};font:700 9px 'Segoe UI',Arial;letter-spacing:.8px;color:{MUT};text-transform:uppercase">Kaldı</th></tr>"""
    body = ''.join(frow(n, fp, ff, ix == len(items) - 1) for ix, (n, fp, ff) in enumerate(items)) or \
           f'<tr><td colspan="3" style="padding:12px 0;font:11px \'Segoe UI\',Arial;color:{MUT}">Feature özeti bulunamadı.</td></tr>'

    return f"""<!doctype html><html><body style="margin:0;background:{BG};padding:24px 0">
<table width="100%" cellpadding="0" cellspacing="0"><tr><td align="center">
<table width="600" cellpadding="0" cellspacing="0" style="background:#ffffff;border:1px solid {LINE};border-radius:8px;overflow:hidden">
 <tr><td style="padding:22px 28px 16px">
   <table width="100%"><tr>
     <td align="left" style="vertical-align:middle"><img src="{logo_ref}" width="138" alt="FaturaLab" style="display:block"></td>
     <td align="right" style="vertical-align:middle">
       <span style="font:600 9px 'Segoe UI',Arial;letter-spacing:1px;color:{MUT}">ORTAM</span>
       <span style="display:inline-block;margin-left:6px;padding:3px 10px;background:{NAVY};color:#fff;font:700 10px 'Segoe UI',Arial;letter-spacing:1.4px;border-radius:3px">DEV</span>
     </td></tr></table>
 </td></tr>
 <tr><td style="padding:6px 28px 14px;text-align:left;border-bottom:3px solid {NAVY}">
   <div style="font:700 17px 'Segoe UI',Arial;color:{INK};letter-spacing:.2px">Otomasyon Koşum Raporu</div>
   <div style="font:11px 'Segoe UI',Arial;color:{MUT};margin-top:4px">{esc(JOB)} · Build #{esc(BUILD)} ·
     <span style="font-weight:700;color:{res_col}">{res_txt}</span></div>
 </td></tr>
 <tr><td style="padding:16px 28px 0;text-align:left">
   <div style="font:11px 'Segoe UI',Arial;color:{MUT}">
     <b style="font-size:14px;color:{NAVY}">{total}</b> senaryo &nbsp;·&nbsp;
     <b style="font-size:14px;color:{OK}">{passed}</b> geçti &nbsp;·&nbsp;
     <b style="font-size:14px;color:{CRIT}">{failed}</b> kaldı &nbsp;·&nbsp;
     <b style="font-size:14px;color:{NAVY}">{rate}</b> başarı</div>
 </td></tr>
 <tr><td style="padding:2px 28px 8px">
   <div style="margin:24px 0 6px">
     <span style="font:700 11px 'Segoe UI',Arial;letter-spacing:1.1px;text-transform:uppercase;color:{NAVY}">Feature Özeti</span>
     <span style="font:600 11px 'Segoe UI',Arial;color:{MUT}"> · {len(items)} feature</span></div>
   <table width="100%" cellspacing="0" cellpadding="0">{head}{body}</table>
 </td></tr>
 <tr><td align="center" style="padding:10px 28px 24px">
   <a href="{REPORT_URL}" style="display:inline-block;background:{NAVY};color:#fff;text-decoration:none;
      padding:11px 26px;border-radius:5px;font:700 12px 'Segoe UI',Arial;letter-spacing:.4px">Detaylı Raporu Görüntüle ↗</a>
 </td></tr>
 <tr><td style="padding:16px 28px 22px;background:{HEADBG};border-top:1px solid {LINE};text-align:left">
   <div style="font:11px/1.6 'Segoe UI',Arial;color:{MUT}">
     <b style="color:{INK}">FaturaLab QA</b> &nbsp;·&nbsp; Otomatik test koşum raporu (Selenium + Cucumber)<br>
     Kaynak: Jenkins ({esc(JOB)}) &nbsp;·&nbsp; Rapor VPN içi dahili sunucudan yayınlanır.<br>
     Oluşturma: {gen_dt} (TR)</div>
 </td></tr>
</table>
<div style="font:10px 'Segoe UI',Arial;color:#9aa5b1;margin-top:10px">FaturaLab · otomatik gönderim</div>
</td></tr></table></body></html>"""


def main():
    total, passed, failed, features = parse_cucumber()
    logo = load_logo_b64()
    logo_ref = 'cid:faturalablogo' if logo else 'https://web.faturalab.com/wp-content/uploads/2022/06/logo.svg'
    html = build_html(total, passed, failed, features, logo_ref)
    subj = f"[FaturaLab QA] Otomasyon Koşum Raporu — {passed}/{total} geçti — Build #{BUILD}"

    root = MIMEMultipart('related')
    root['Subject'] = subj
    root['From'] = MAIL_FROM
    root['To'] = ', '.join(MAIL_TO)
    if MAIL_CC:
        root['Cc'] = ', '.join(MAIL_CC)
    root['Date'] = formatdate(localtime=True)
    root['Message-ID'] = make_msgid(domain='ileti.faturalab.com')
    # Mail istemcisinde klasorleme icin ayirt edici header (Outlook kurali: bu header'a gore filtrele)
    root['X-FaturaLab-Report'] = 'qa-automation'
    root.attach(MIMEText(html, 'html', 'utf-8'))
    if logo:
        img = MIMEImage(base64.b64decode(logo), 'png')
        img.add_header('Content-ID', '<faturalablogo>')
        img.add_header('Content-Disposition', 'inline', filename='faturalab.png')
        root.attach(img)

    try:
        s = smtplib.SMTP(SMTP_HOST, SMTP_PORT, timeout=20)
        s.ehlo()
        s.sendmail(MAIL_FROM, MAIL_TO + MAIL_CC, root.as_string())
        s.quit()
        print(f'[OK] rapor maili gonderildi -> TO {MAIL_TO} CC {MAIL_CC} '
              f'(ozet: {passed}/{total} gecti, {failed} kaldi)')
    except Exception as e:
        print(f'[HATA] mail gonderilemedi: {e!r}')
        sys.exit(0)  # mail hatasi build'i dusurmesin


if __name__ == '__main__':
    main()
