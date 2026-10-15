package com.faturalab.automation.utils.parity;

import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.selenium.AxeBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.faturalab.automation.utils.parity.ParityFinding.Severity;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Erişilebilirlik katmanı (K4): axe-core ile WCAG 2.2 AA taraması.
 * Referans = eski tasarımın ihlal listesi (spec {@code rules.axeBaseline}); yeni ekran yeni ihlal getiremez.
 */
public final class AxeCheck {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final List<String> WCAG_TAGS = Arrays.asList("wcag2a", "wcag2aa", "wcag21a", "wcag21aa", "wcag22aa");
    private static final String[] IMPACTS = {"critical", "serious", "moderate", "minor"};

    private AxeCheck() {
    }

    /** Taramayı çalıştırır, etki seviyesine göre sayılmış özet döner (reCAPTCHA vb. iframe'ler hariç). */
    public static ObjectNode scan(WebDriver driver) {
        Results results = new AxeBuilder().withTags(WCAG_TAGS).disableIframeTesting().analyze(driver);
        if (results.isErrored()) {
            throw new IllegalStateException("axe taraması hata verdi: " + results.getErrorMessage());
        }
        ObjectNode summary = MAPPER.createObjectNode();
        ObjectNode byImpact = summary.putObject("byImpact");
        for (String i : IMPACTS) {
            byImpact.put(i, 0);
        }
        ArrayNode rules = summary.putArray("rules");
        for (Rule r : results.getViolations()) {
            String impact = r.getImpact() == null ? "minor" : r.getImpact();
            byImpact.put(impact, byImpact.path(impact).asInt() + 1);
            rules.addObject().put("id", r.getId()).put("impact", impact)
                    .put("nodes", r.getNodes() == null ? 0 : r.getNodes().size()).put("help", r.getHelp());
        }
        summary.put("total", results.getViolations().size());
        return summary;
    }

    /**
     * Kural bazında karşılaştırma (etki seviyesine göre sayı eşiği yanıltıcıdır: farklı kurallar aynı sayıyı verebilir).
     * Eskide olmayan kural veya aynı kuralda etkilenen öğe sayısının artması: critical/serious → FAIL, diğerleri → WARN.
     * Eskide olup yenide kalkan ihlal → INFO.
     *
     * @param baselineRules eski tasarımın ihlal listesi: [{"id","impact","nodes"}]
     */
    public static List<ParityFinding> compare(JsonNode summary, JsonNode baselineRules) {
        Map<String, Integer> oldNodes = new HashMap<>();
        for (JsonNode r : baselineRules) {
            oldNodes.put(r.path("id").asText(), r.path("nodes").asInt());
        }
        List<ParityFinding> out = new ArrayList<>();
        for (JsonNode r : summary.path("rules")) {
            String id = r.path("id").asText();
            String impact = r.path("impact").asText();
            int nodes = r.path("nodes").asInt();
            Severity sev = ("critical".equals(impact) || "serious".equals(impact)) ? Severity.FAIL : Severity.WARN;
            Integer was = oldNodes.remove(id);
            if (was == null) {
                out.add(new ParityFinding(sev, "axe", "Yeni ihlal [" + impact + "] " + id + " (" + nodes + " öğe) — " + r.path("help").asText()));
            } else if (nodes > was) {
                out.add(new ParityFinding(sev, "axe", "İhlal arttı [" + impact + "] " + id + ": " + was + " → " + nodes + " öğe — " + r.path("help").asText()));
            }
        }
        oldNodes.forEach((id, n) -> out.add(new ParityFinding(Severity.INFO, "axe", "Eskideki ihlal giderilmiş: " + id + " (" + n + " öğe)")));
        return out;
    }
}
