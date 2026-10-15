package com.faturalab.automation.utils.parity;

import com.fasterxml.jackson.databind.JsonNode;
import com.faturalab.automation.utils.parity.ParityFinding.Severity;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Eski tasarımın dondurulmuş parmak izi (baseline) ile yeni ekranın canlı parmak izini karşılaştırır.
 *
 * <p>Kurallar (spec dosyası {@code testdata/parity/<ekran>.json > rules}):
 * <ul>
 *   <li>Eksik alan / buton / etiketli checkbox → FAIL</li>
 *   <li>Alanın zorunluluğu, password tipi veya maxlength'i kaybolmuş → FAIL; maxlength değeri değişmiş → WARN</li>
 *   <li>Buton yenide sadece tıklanabilir metin (span/div, tabindex/role yok) olarak var → FAIL (klavye erişimi kaybı)</li>
 *   <li>Link kaybı → WARN; yenide baseline'da olmayan alan → INFO</li>
 * </ul>
 * Tenant'a özgü/bilinçli farklar {@code ignore*} listeleriyle, yeniden adlandırmalar {@code *Aliases} ile tanımlanır.
 */
public final class ParityAssert {

    private static final String KEYBOARD_MARK = "[klavye-erisilemez]";

    private ParityAssert() {
    }

    public static List<ParityFinding> compare(JsonNode spec, JsonNode live) {
        JsonNode baseline = spec.path("baseline");
        JsonNode rules = spec.path("rules");
        List<ParityFinding> out = new ArrayList<>();
        if (baseline.path("fields").isEmpty() && baseline.path("buttons").isEmpty()) {
            // Boş/bozuk spec sessizce "uyumlu" sonucu vermesin
            out.add(new ParityFinding(Severity.FAIL, "spec", "Baseline'da hiç alan/buton yok — spec dosyası boş veya bozuk"));
            return out;
        }
        compareFields(baseline.path("fields"), live.path("fields"), rules, out);
        compareButtons(baseline.path("buttons"), live, rules, out);
        compareCheckboxes(baseline.path("checkboxes"), live.path("checkboxes"), out);
        compareLinks(baseline.path("links"), live.path("links"), rules, out);
        return out;
    }

    // ---------------------------------------------------------------- alanlar
    private static void compareFields(JsonNode oldFields, JsonNode newFields, JsonNode rules, List<ParityFinding> out) {
        Map<String, String> aliases = aliasMap(rules.path("fieldAliases"));
        Set<String> ignored = nameSet(rules.path("ignoreFields"));
        Map<String, JsonNode> liveByName = new HashMap<>();
        for (JsonNode f : newFields) {
            liveByName.put(fieldName(f), f);
        }
        Set<String> matched = new HashSet<>();
        for (JsonNode oldF : oldFields) {
            String oldName = fieldName(oldF);
            if (ignored.contains(oldName)) {
                continue;
            }
            String target = aliases.getOrDefault(oldName, oldName);
            JsonNode newF = liveByName.get(target);
            if (newF == null) {
                out.add(new ParityFinding(Severity.FAIL, "alan", "Eksik alan: '" + displayName(oldF) + "'"
                        + (target.equals(oldName) ? "" : " (beklenen yeni ad: '" + target + "')")));
                continue;
            }
            matched.add(target);
            compareFieldRules(displayName(oldF), oldF, newF, out);
        }
        liveByName.forEach((name, f) -> {
            if (!matched.contains(name)) {
                out.add(new ParityFinding(Severity.INFO, "alan", "Yeni alan (baseline'da yok): '" + displayName(f) + "'"));
            }
        });
    }

    private static void compareFieldRules(String name, JsonNode oldF, JsonNode newF, List<ParityFinding> out) {
        if (oldF.path("required").asBoolean() && !newF.path("required").asBoolean()) {
            out.add(new ParityFinding(Severity.FAIL, "alan", "'" + name + "' zorunluluğu kaybolmuş"));
        }
        if ("password".equals(oldF.path("type").asText()) && !"password".equals(newF.path("type").asText())) {
            out.add(new ParityFinding(Severity.FAIL, "alan", "'" + name + "' artık password tipinde değil (şifre açık görünür)"));
        }
        String oldMax = maxlength(oldF);
        String newMax = maxlength(newF);
        if (oldMax != null && newMax == null) {
            out.add(new ParityFinding(Severity.FAIL, "alan", "'" + name + "' maxlength kaybolmuş (eski: " + oldMax + ", yeni: sınırsız)"));
        } else if (oldMax != null && !oldMax.equals(newMax)) {
            out.add(new ParityFinding(Severity.WARN, "alan", "'" + name + "' maxlength değişmiş (eski: " + oldMax + ", yeni: " + newMax + ")"));
        }
    }

    // ---------------------------------------------------------------- butonlar
    private static void compareButtons(JsonNode oldButtons, JsonNode live, JsonNode rules, List<ParityFinding> out) {
        Map<String, String> aliases = aliasMap(rules.path("buttonAliases"));
        Set<String> ignored = nameSet(rules.path("ignoreButtons"));
        Set<String> liveButtons = new HashSet<>();
        for (JsonNode b : live.path("buttons")) {
            liveButtons.add(norm(b.asText()));
        }
        Map<String, Boolean> pseudo = new HashMap<>(); // ad → klavyeyle erişilemez mi
        for (JsonNode p : live.path("pseudoClickables")) {
            String txt = p.asText();
            // aynı metin hem erişilebilir hem erişilemez geçebilir — erişilemez olan kazanır
            pseudo.merge(norm(txt.replace(KEYBOARD_MARK, "")), txt.contains(KEYBOARD_MARK), Boolean::logicalOr);
        }
        for (JsonNode b : oldButtons) {
            String oldName = norm(b.asText());
            if (oldName.isEmpty() || ignored.contains(oldName)) {
                continue;
            }
            String target = aliases.getOrDefault(oldName, oldName);
            if (liveButtons.contains(target)) {
                continue;
            }
            if (Boolean.TRUE.equals(pseudo.get(target))) {
                out.add(new ParityFinding(Severity.FAIL, "erisilebilirlik", "'" + b.asText()
                        + "' eskide butondu, yenide klavyeyle erişilemeyen tıklanabilir metin (tabindex/role yok)"));
            } else if (pseudo.containsKey(target)) {
                out.add(new ParityFinding(Severity.WARN, "buton", "'" + b.asText() + "' buton değil, rol atanmış metin olarak duruyor"));
            } else {
                out.add(new ParityFinding(Severity.FAIL, "buton", "Eksik buton/aksiyon: '" + b.asText() + "'"));
            }
        }
    }

    // ---------------------------------------------------------------- checkbox & link
    private static void compareCheckboxes(JsonNode oldCbs, JsonNode newCbs, List<ParityFinding> out) {
        Map<String, JsonNode> live = new HashMap<>();
        for (JsonNode c : newCbs) {
            live.put(norm(c.path("label").asText()), c);
        }
        int oldUnlabeled = 0;
        for (JsonNode c : oldCbs) {
            String label = norm(c.path("label").asText());
            if (label.isEmpty()) {
                oldUnlabeled++;
                continue;
            }
            JsonNode n = live.get(label);
            if (n == null) {
                out.add(new ParityFinding(Severity.FAIL, "checkbox", "Eksik checkbox: '" + c.path("label").asText() + "'"));
            } else if (c.path("required").asBoolean() && !n.path("required").asBoolean()) {
                out.add(new ParityFinding(Severity.FAIL, "checkbox", "'" + c.path("label").asText() + "' zorunluluğu kaybolmuş"));
            }
        }
        if (oldUnlabeled > newCbs.size()) {
            out.add(new ParityFinding(Severity.WARN, "checkbox", "Etiketsiz checkbox sayısı azalmış (eski " + oldUnlabeled + ", yeni toplam " + newCbs.size() + ")"));
        }
    }

    private static void compareLinks(JsonNode oldLinks, JsonNode newLinks, JsonNode rules, List<ParityFinding> out) {
        Set<String> ignored = new HashSet<>();
        for (JsonNode n : rules.path("ignoreLinks")) {
            ignored.add(linkKey(n.isObject() ? n.path("name").asText() : n.asText()));
        }
        Set<String> live = new HashSet<>();
        for (JsonNode l : newLinks) {
            live.add(linkKey(l.asText()));
        }
        for (JsonNode l : oldLinks) {
            String key = linkKey(l.asText());
            if (!ignored.contains(key) && !live.contains(key)) {
                out.add(new ParityFinding(Severity.WARN, "link", "Link kaybolmuş: '" + l.asText() + "' (anahtar: " + key + ")"));
            }
        }
    }

    /** mailto linkleri tenant'tan bağımsız tek anahtara indirgenir; diğerleri href'e göre. */
    static String linkKey(String link) {
        int arrow = link.lastIndexOf("->");
        String href = arrow >= 0 ? link.substring(arrow + 2).trim() : link.trim();
        return href.startsWith("mailto:") ? "mailto" : norm(href);
    }

    // ---------------------------------------------------------------- yardımcılar
    static String fieldName(JsonNode f) {
        return norm(displayName(f));
    }

    /** Ekranda görünen ad: etiket, yoksa placeholder. */
    private static String displayName(JsonNode f) {
        String label = f.path("label").asText("");
        return label.isEmpty() ? f.path("placeholder").asText("") : label;
    }

    private static String maxlength(JsonNode f) {
        JsonNode m = f.path("maxlength");
        return m.isMissingNode() || m.isNull() || m.asText().isEmpty() ? null : m.asText();
    }

    /** Türkçe-duyarsız, büyük/küçük harf ve "(Zorunlu)" ekinden bağımsız ad. */
    public static String norm(String s) {
        if (s == null) {
            return "";
        }
        String t = Normalizer.normalize(s, Normalizer.Form.NFC).toLowerCase(new Locale("tr", "TR"))
                .replace("(zorunlu)", "")
                .replace('ı', 'i').replace('ş', 's').replace('ğ', 'g')
                .replace('ü', 'u').replace('ö', 'o').replace('ç', 'c').replace("i̇", "i");
        return t.replaceAll("\\s+", " ").trim();
    }

    private static Map<String, String> aliasMap(JsonNode node) {
        Map<String, String> m = new HashMap<>();
        node.fields().forEachRemaining(e -> m.put(norm(e.getKey()), norm(e.getValue().asText())));
        return m;
    }

    /** ignore listeleri: ["ad"] veya [{"name":"ad","reason":"..."}] kabul eder. */
    private static Set<String> nameSet(JsonNode arr) {
        Set<String> s = new HashSet<>();
        for (JsonNode n : arr) {
            String name = norm(n.isObject() ? n.path("name").asText() : n.asText());
            if (!name.isEmpty()) { // boş ignore girdisi etiketsiz alanları sessizce yutmasın
                s.add(name);
            }
        }
        return s;
    }
}
