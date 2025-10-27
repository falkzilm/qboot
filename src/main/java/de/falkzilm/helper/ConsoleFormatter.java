package de.falkzilm.helper;

import com.sun.tools.javac.Main;
import de.falkzilm.gen.Framework;
import org.eclipse.microprofile.config.ConfigProvider;
import picocli.CommandLine.Help;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConsoleFormatter {

    /** Breite der Label-Spalte (anpassbar) */
    private static final int LABEL_W = 10;

    public static CheckHandle beginCheck(String label, boolean optional, String required) {
        var h = new CheckHandle(label, optional, required);
        h.printStart();
        return h;
    }

    public static final class CheckHandle {
        private final String label;
        private final boolean optional;
        private final String required;
        private final Instant t0 = Instant.now();
        private String detected;

        private static final String CLR = "\u001B[2K"; // clear line
        private static final String CR  = "\r";

        private CheckHandle(String label, boolean optional, String required) {
            this.label = label; this.optional = optional; this.required = required;
        }

        /** während des Checks z. B. erkannte Version setzen */
        public CheckHandle detected(String value) { this.detected = value; return this; }

        public void printStart() {
            String opt = optional ? tag("faint","(optional)") : tag("bold","required");
            String icon = tag("cyan","⧗");
            String req  = (required==null||required.isBlank()) ? "" : "  " + tag("faint","req: "+required);

            String line = String.format("  %-"+LABEL_W+"s  %s Checking…  %s%s",
                    label, icon, opt, req);

            System.out.print(Help.Ansi.AUTO.string(CR + CLR + line));
            System.out.flush();
        }

        public void ok() { finish(true, null); }
        public void ok(String note) { finish(true, note); }
        public void fail(String reason) { finish(false, reason); }

        private void finish(boolean success, String note) {
            String mark = success ? tag("green","✅") : tag("red","❌");
            String opt  = optional ? tag("faint","(optional)") : tag("bold","required");
            String req  = (required==null||required.isBlank()) ? "" : "  " + tag("faint","req: "+required);
            String det  = (detected==null||detected.isBlank()) ? "" : "  " + tag("faint","found: "+detected);
            String tail = (note==null||note.isBlank()) ? "" : "  " + tag("faint",note);
            String took = tag("faint",humanTime(Duration.between(t0, Instant.now())));

            String line = String.format("  %-"+LABEL_W+"s  %s  %s  %s%s%s  %s",
                    label, mark, opt, req, det, tail, took);

            System.out.print(Help.Ansi.AUTO.string(CR + CLR + line) + System.lineSeparator());
            System.out.flush();
        }

        private static String tag(String style, String text) {
            return "@|"+style+" "+text+"|@";
        }
    }


    public static String humanTime(Duration d) {
        if (d == null) return "n/a";
        long ms = d.toMillis();
        if (ms < 1000) return ms + " ms";
        long s = ms / 1000;
        long rem = ms % 1000;
        return s + "." + String.format("%03d", rem) + " s";
    }

    public static void footer(String destinationDir, Duration took) {
        String ansi = Help.Ansi.AUTO.string(String.join("\n",
                "@|bold,green  ✅  Generation complete!|@",
                "@|bold,white  ─────────────────────────────────────────|@",
                " @|green •|@ Target        : @|bold " + destinationDir + "|@",
                " @|green •|@ Duration      : @|bold " + humanTime(took) + "|@",
                "",
                "@|bold,magenta  🎉  Ready to code:|@ @|cyan cd " + destinationDir + "|@",
                ""
        ));
        System.out.println(ansi);
    }

    public static void header(Framework framework, String frameworkVersion, String dir) {
        String s = Help.Ansi.AUTO.string(String.join("\n",
                "@|bold,white ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓|@",
                "@|bold,white ┃|@  @|bold,green 🚀 Bootstrapping " + framework.label + "(" + frameworkVersion + ")" +" project|@",
                "@|bold,white ┃|@    @|faint in|@ @|cyan " + dir + "|@",
                "@|bold,white ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛|@"
        ));
        System.out.println(s);
    }

    public static void section(String title) {
        System.out.println(Help.Ansi.AUTO.string(
                "@|bold,white " + title + "|@\n@|faint ─────────────────────────────────────────────|@"));
    }

    public static void success(String msg) {
        System.out.println(Help.Ansi.AUTO.string(
                "@|bold,green ✅ " + msg + "|@\n"));
    }

    public static void bullet(String txt) {
        System.out.println(Help.Ansi.AUTO.string("  @|cyan •|@ " + txt));
    }

    public static void subbullet(String txt) {
        System.out.println(Help.Ansi.AUTO.string("    @|magenta •|@ " + txt));
    }

    public static void error(String title, String details, Throwable cause, String... tips) {
        // kompakte Cause-Chain (nur Messages)
        var causeMsg = (cause == null) ? null : compactCause(cause);

        String boxTop = "@|bold,white ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓|@";
        String boxMid = "@|bold,white ┃|@  @|bold,red ✖ " + esc(title) + "|@";
        String boxBot = "@|bold,white ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛|@";

        var b = new StringBuilder();
        b.append(boxTop).append('\n')
                .append(boxMid).append('\n')
                .append(boxBot).append('\n');

        if (details != null && !details.isBlank()) {
            b.append("  ").append("@|red ").append(esc(details)).append("|@").append('\n');
        }
        if (causeMsg != null && !causeMsg.isBlank()) {
            b.append("  ").append("@|faint cause: ").append(esc(causeMsg)).append("|@").append('\n');
        }
        if (tips != null && tips.length > 0) {
            b.append(Help.Ansi.AUTO.string("@|bold,white Hints:|@")).append('\n');
            for (var tip : tips) {
                if (tip != null && !tip.isBlank()) {
                    b.append(Help.Ansi.AUTO.string("  @|yellow •|@ " + esc(tip))).append('\n');
                }
            }
        }
        System.err.println(Help.Ansi.AUTO.string(b.toString()));
    }

    // Kompakter Debug-Header + optionale Details (Key/Value + freie Zeilen)
    public static void debug(String title, Map<String, ?> details, String... lines) {

        String ts = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SSS")
                .withZone(ZoneId.systemDefault()).format(Instant.now());

        var b = new StringBuilder();
        b.append("@|faint ").append(ts).append("|@ ")
                .append("@|bold,cyan 🔎 DEBUG|@ ")
                .append("@|cyan ").append(esc(title)).append("|@").append('\n');

        if (details != null && !details.isEmpty()) {
            int w = details.keySet().stream().mapToInt(k -> String.valueOf(k).length()).max().orElse(8);
            for (var e : details.entrySet()) {
                String k = String.valueOf(e.getKey());
                String v = String.valueOf(e.getValue());
                b.append(String.format("  %-" + w + "s : %s%n", k, v));
            }
        }
        if (lines != null) {
            for (var ln : lines) if (ln != null && !ln.isBlank()) {
                b.append("  ").append(esc(ln)).append('\n');
            }
        }
        System.err.print(Help.Ansi.AUTO.string(b.toString()));
    }

    // Bequeme Overloads
    public static void debug(String title, String... lines) {
        debug(title, Collections.emptyMap(), lines);
    }
    public static void debugKV(String title, Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put(String.valueOf(kv[i]), kv[i+1]);
        debug(title, m);
    }

    public static void bannerRocketBox() {
         String s = Help.Ansi.AUTO.string(String.join("\n",
                 "@|bold,white ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓|@",
                "@|bold,white ┃|@  @|bold,green 🚀 qBoot|@  @|faint,white — Bootstrap anything.|@",
                "@|bold,white ┃|@  @|faint,white Your helper to create projects from templates.|@",
                "@|bold,white ┃|@  @|yellow v" + detectVersion() + "|@",
                 "@|bold,white ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛|@"
        ));
         System.out.println(s);
    }

    public static String detectVersion() {
        return ConfigProvider.getConfig()
                .getOptionalValue("quarkus.application.version", String.class)
                .orElse("dev");
    }

    /** Kürzt die Exception-Chain zu "TopMessage ⇢ Cause ⇢ …" */
    private static String compactCause(Throwable t) {
        var parts = new java.util.ArrayList<String>(4);
        for (Throwable c = t; c != null; c = c.getCause()) {
            var msg = c.getMessage();
            var simple = c.getClass().getSimpleName();
            parts.add((msg == null || msg.isBlank()) ? simple : simple + ": " + msg);
            if (parts.size() == 4) break;
        }
        return String.join(" ⇢ ", parts);
    }

    /** ent-schmückt ANSI-Klammern-Sonderfälle */
    private static String esc(String s) {
        return s == null ? "" : s.replace("@", "@@"); // picocli-ANSI escaping
    }

    private static String join(String... parts) {
        var sb = new StringBuilder();
        for (var p : parts) if (p != null && !p.isBlank()) {
            if (!sb.isEmpty()) sb.append("  ");
            sb.append(p);
        }
        return sb.toString();
    }
}
