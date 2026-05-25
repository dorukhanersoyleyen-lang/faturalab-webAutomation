package com.faturalab.automation.utils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Vaadin diyalog / form içindeki alanların değerlerini JSON ile kaydeder ve geri yükler (UAT teardown).
 */
public final class VaadinFormFieldSnapshot {

    private VaadinFormFieldSnapshot() {
    }

    private static final String READ_VAL =
            "function readVal(host){var v=host.value;if(v!=null&&String(v).trim()!=='')return String(v).trim();"
                    + "var inp=host.querySelector('input, textarea');if(inp&&inp.value)return String(inp.value).trim();"
                    + "return '';}";

    private static final String SET_HOST =
            "function setHostValue(host,text){if(!host||host.disabled||host.hasAttribute('disabled'))return false;"
                    + "try{host.focus();}catch(e){}var s=text==null?'':String(text);host.value=s;"
                    + "var inp=host.querySelector('input, textarea');if(inp){inp.value=s;"
                    + "try{inp.dispatchEvent(new Event('input',{bubbles:true}));}catch(e2){}}"
                    + "try{host.dispatchEvent(new Event('input',{bubbles:true,composed:true}));}catch(e3){}"
                    + "try{host.dispatchEvent(new CustomEvent('value-changed',{bubbles:true,composed:true,"
                    + "detail:{value:s}}));}catch(e4){}return true;}";

    private static final String FIELD_SEL =
            "vaadin-text-field, vaadin-text-area, vaadin-email-field, vaadin-integer-field, "
                    + "vaadin-number-field, vaadin-date-picker, vaadin-select, vaadin-combo-box, "
                    + "vaadin-multi-select-combo-box";

    /** Açık firma düzenleme diyaloğu (veya son açılan overlay). */
    public static final String ROOT_FIRMA_EDIT =
            "(function(){var o=document.querySelector('vaadin-dialog-overlay[opened]');"
                    + "if(!o)o=document.querySelector('vaadin-dialog-overlay');return o;})()";

    /**
     * Kullanıcı düzenleme: önce açık diyalog, yoksa sayfadaki ilk form layout veya body (Yalnızca görünür alanlar).
     */
    public static final String ROOT_KULLANICI_EDITOR =
            "(function(){var o=document.querySelector('vaadin-dialog-overlay[opened]');if(o)return o;"
                    + "var f=document.querySelector('vaadin-form-layout');if(f)return f;"
                    + "return document.body;})()";

    public static String snapshot(WebDriver driver, String rootExpr) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Object o = js.executeScript(
                READ_VAL
                        + "var root = "
                        + rootExpr
                        + ";"
                        + "if (!root) return null;"
                        + "var arr = [];"
                        + "root.querySelectorAll('"
                        + FIELD_SEL
                        + "').forEach(function(host) {"
                        + "  if (host.disabled || host.hasAttribute('disabled') || host.hasAttribute('readonly')) return;"
                        + "  arr.push({tag: host.tagName.toLowerCase(), label: host.getAttribute('label')||'',"
                        + "    value: readVal(host), prop: host.value != null ? String(host.value) : ''});"
                        + "});"
                        + "return JSON.stringify(arr);");
        return o == null ? null : String.valueOf(o);
    }

    public static void restore(WebDriver driver, String json, String rootExpr) {
        if (json == null || json.isBlank() || "null".equals(json)) {
            return;
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
                SET_HOST
                        + "var root = "
                        + rootExpr
                        + ";"
                        + "if (!root) return;"
                        + "var data = JSON.parse(arguments[0]);"
                        + "if (!data || !data.length) return;"
                        + "var hosts = [];"
                        + "root.querySelectorAll('"
                        + FIELD_SEL
                        + "').forEach(function(host) {"
                        + "  if (host.disabled || host.hasAttribute('disabled') || host.hasAttribute('readonly')) return;"
                        + "  hosts.push(host);"
                        + "});"
                        + "var used = {};"
                        + "data.forEach(function(d) {"
                        + "  for (var i = 0; i < hosts.length; i++) {"
                        + "    if (used[i]) continue;"
                        + "    var h = hosts[i];"
                        + "    if (h.tagName.toLowerCase() !== d.tag) continue;"
                        + "    var lab = h.getAttribute('label')||'';"
                        + "    if (d.label && lab !== d.label) continue;"
                        + "    var tag = d.tag;"
                        + "    var toSet = (tag.indexOf('select') >= 0 || tag.indexOf('combo') >= 0)"
                        + "      ? (d.prop || d.value) : d.value;"
                        + "    setHostValue(h, toSet);"
                        + "    used[i] = true;"
                        + "    break;"
                        + "  }"
                        + "});",
                json);
    }
}
