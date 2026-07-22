# -*- coding: utf-8 -*-
"""
FaturaLab QA Otomasyon - test raporu mail gonderici (Jenkins pipeline'dan cagrilir).
Cucumber JSON'dan ozet cikarir, guzel bir HTML mail olusturur ve ileti relay'inden gonderir.

Env degiskenleri (Jenkins saglar):
  JOB_NAME, BUILD_NUMBER, BUILD_URL, BUILD_RESULT (SUCCESS/UNSTABLE/FAILURE)
  REPORT_URL   - Netlify public link (yoksa BUILD_URL fallback)
  MAIL_TO      - virgullu alici listesi (varsayilan: 3 kisi)
Calisma dizini repo koku (target/cucumber-reports/*.json okunur).
"""
import os, sys, glob, json, smtplib
from email.mime.text import MIMEText
from email.utils import formatdate, make_msgid
sys.stdout.reconfigure(encoding='utf-8', errors='replace')

SMTP_HOST = os.environ.get('SMTP_HOST', 'ileti.faturalab.com')
SMTP_PORT = int(os.environ.get('SMTP_PORT', '25'))
MAIL_FROM = os.environ.get('MAIL_FROM', 'automationReport-noreply@ileti.faturalab.com')
MAIL_TO = [x.strip() for x in os.environ.get(
    'MAIL_TO',
    'dorukhan.ersoyleyen@faturalab.com,huseyin.taskin@faturalab.com,ramazan.okul@faturalab.com'
).split(',') if x.strip()]

JOB = os.environ.get('JOB_NAME', 'faturalab-webAutomation-pipeline')
BUILD = os.environ.get('BUILD_NUMBER', '?')
BUILD_URL = os.environ.get('BUILD_URL', '')
RESULT = os.environ.get('BUILD_RESULT', 'UNKNOWN').upper()


def _report_url():
    # 1) Netlify deploy ciktisi (netlify-out.json), 2) REPORT_URL env, 3) BUILD_URL fallback
    try:
        d = json.load(open('netlify-out.json', encoding='utf-8'))
        u = d.get('deploy_ssl_url') or d.get('deploy_url') or d.get('url')
        if u:
            return u
    except Exception:
        pass
    return os.environ.get('REPORT_URL', '') or BUILD_URL


REPORT_URL = _report_url()


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


def build_html(total, passed, failed, features):
    color = {'SUCCESS': '#2e7d32', 'UNSTABLE': '#f9a825', 'FAILURE': '#c62828'}.get(RESULT, '#546e7a')
    badge = {'SUCCESS': 'BASARILI', 'UNSTABLE': 'KARARSIZ (bazi testler basarisiz)',
             'FAILURE': 'BASARISIZ'}.get(RESULT, RESULT)
    rate = f'{(passed/total*100):.0f}%' if total else '-'
    rows = ''
    for name, fp, ff in sorted(features, key=lambda x: -x[2]):
        st = '#c62828' if ff else '#2e7d32'
        rows += (f'<tr><td style="padding:6px 10px;border-bottom:1px solid #eee">{name}</td>'
                 f'<td style="padding:6px 10px;border-bottom:1px solid #eee;text-align:center;color:#2e7d32">{fp}</td>'
                 f'<td style="padding:6px 10px;border-bottom:1px solid #eee;text-align:center;color:{st};font-weight:bold">{ff}</td></tr>')
    return f"""<!doctype html><html><body style="margin:0;background:#f4f6f8;font-family:Segoe UI,Arial,sans-serif;color:#263238">
<div style="max-width:640px;margin:0 auto;padding:24px">
  <div style="background:#0d47a1;border-radius:10px 10px 0 0;padding:22px 26px">
    <div style="color:#fff;font-size:20px;font-weight:700">FaturaLab QA Otomasyon</div>
    <div style="color:#bbdefb;font-size:13px;margin-top:2px">API &amp; Fatura Yukleme Test Raporu</div>
  </div>
  <div style="background:#fff;border-radius:0 0 10px 10px;padding:26px;box-shadow:0 1px 3px rgba(0,0,0,.1)">
    <div style="display:inline-block;background:{color};color:#fff;font-weight:700;padding:6px 14px;border-radius:20px;font-size:13px">{badge}</div>
    <table style="width:100%;margin:18px 0;border-collapse:collapse">
      <tr>
        <td style="text-align:center;padding:10px;background:#f4f6f8;border-radius:8px">
          <div style="font-size:26px;font-weight:700">{total}</div><div style="font-size:12px;color:#607d8b">Toplam</div></td>
        <td style="width:10px"></td>
        <td style="text-align:center;padding:10px;background:#e8f5e9;border-radius:8px">
          <div style="font-size:26px;font-weight:700;color:#2e7d32">{passed}</div><div style="font-size:12px;color:#607d8b">Gecti</div></td>
        <td style="width:10px"></td>
        <td style="text-align:center;padding:10px;background:#ffebee;border-radius:8px">
          <div style="font-size:26px;font-weight:700;color:#c62828">{failed}</div><div style="font-size:12px;color:#607d8b">Kaldi</div></td>
        <td style="width:10px"></td>
        <td style="text-align:center;padding:10px;background:#f4f6f8;border-radius:8px">
          <div style="font-size:26px;font-weight:700">{rate}</div><div style="font-size:12px;color:#607d8b">Basari</div></td>
      </tr>
    </table>
    <div style="text-align:center;margin:22px 0">
      <a href="{REPORT_URL}" style="background:#0d47a1;color:#fff;text-decoration:none;padding:12px 28px;border-radius:8px;font-weight:700;font-size:15px;display:inline-block">Detayli Raporu Goruntule &rarr;</a>
    </div>
    <table style="width:100%;border-collapse:collapse;font-size:13px;margin-top:10px">
      <thead><tr style="background:#eceff1"><th style="padding:8px 10px;text-align:left">Feature</th><th style="padding:8px 10px">Gecti</th><th style="padding:8px 10px">Kaldi</th></tr></thead>
      <tbody>{rows or '<tr><td colspan=3 style="padding:10px;color:#607d8b">Feature ozeti yok</td></tr>'}</tbody>
    </table>
    <div style="margin-top:20px;font-size:12px;color:#90a4ae;border-top:1px solid #eee;padding-top:14px">
      Job: {JOB} &nbsp;|&nbsp; Build #{BUILD} &nbsp;|&nbsp; Ortam: Dev<br>
      Bu mail FaturaLab QA otomasyon pipeline'i tarafindan otomatik gonderilmistir.
    </div>
  </div>
</div></body></html>"""


def main():
    total, passed, failed, features = parse_cucumber()
    html = build_html(total, passed, failed, features)
    subj = f"[FaturaLab QA] API Test Raporu - {RESULT} ({passed}/{total} gecti) - Build #{BUILD}"
    msg = MIMEText(html, 'html', 'utf-8')
    msg['Subject'] = subj
    msg['From'] = MAIL_FROM
    msg['To'] = ', '.join(MAIL_TO)
    msg['Date'] = formatdate(localtime=True)
    msg['Message-ID'] = make_msgid(domain='ileti.faturalab.com')
    try:
        s = smtplib.SMTP(SMTP_HOST, SMTP_PORT, timeout=20)
        s.ehlo()
        s.sendmail(MAIL_FROM, MAIL_TO, msg.as_string())
        s.quit()
        print(f'[OK] rapor maili gonderildi -> {MAIL_TO} (ozet: {passed}/{total} gecti, {failed} kaldi)')
    except Exception as e:
        print(f'[HATA] mail gonderilemedi: {e!r}')
        sys.exit(0)  # mail hatasi build'i dusurmesin


if __name__ == '__main__':
    main()
