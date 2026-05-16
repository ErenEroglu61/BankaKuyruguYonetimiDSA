package com.bankqueue.ui;

import java.awt.Color;
import java.awt.Font;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

// Tema düzenleyici theme.json ile beraber çalışır. Ember Dark Default

public class Theme {

    public static Color BG, SURFACE, SURF2, BORDER;
    public static Color PRIMARY, SUCCESS, DANGER, WARNING, PURPLE, TEAL;
    public static Color FG, FG2;
    public static String FONT_FAMILY = "Segoe UI";

    static {
        loadFromJson("BankaKuyruğuSistemi/src/com/bankqueue/config/theme.json");
    }

    //JSON Okuyucu
    private static void loadFromJson(String path) {
        Map<String, String> map = new HashMap<>();
        File f = new File(path);
        if (f.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    // "key": "#rrggbb" satırlarını yakala
                    if (line.startsWith("\"") && line.contains(":") && line.contains("#")) {
                        String[] parts = line.split(":");
                        String key = parts[0].replaceAll("[\"\\s,]", "");
                        String val = parts[1].replaceAll("[\"\\s,]", "");
                        map.put(key, val);
                    }
                    if (line.contains("\"font\"")) {
                        String val = line.split(":")[1].replaceAll("[\"\\s,]", "");
                        FONT_FAMILY = val.isEmpty() ? FONT_FAMILY : val;
                    }
                }
                System.out.println("[Theme] '" + path + "' yüklendi.");
            } catch (IOException e) {
                System.err.println("[Theme] JSON okunamadı, varsayılan kullanılıyor.");
            }
        } else {
            System.out.println("[Theme] theme.json bulunamadı, varsayılan 'Ember Dark' kullanılıyor.");
        }

        BG      = hex(map, "bg",      "#0f0e17");
        SURFACE = hex(map, "surface", "#1f1d2e");
        SURF2   = hex(map, "surf2",   "#2d2b3d");
        BORDER  = hex(map, "border",  "#4a4869");
        PRIMARY = hex(map, "primary", "#ff8906");
        SUCCESS = hex(map, "success", "#2cb67d");
        DANGER  = hex(map, "danger",  "#e53170");
        WARNING = hex(map, "warning", "#ffd166");
        PURPLE  = hex(map, "purple",  "#7c3aed");
        TEAL    = hex(map, "teal",    "#06b6d4");
        FG      = hex(map, "fg",      "#fffffe");
        FG2     = hex(map, "fg2",     "#a7a9be");
    }

    private static Color hex(Map<String, String> map, String key, String fallback) {
        String val = map.getOrDefault(key, fallback);
        try { return Color.decode(val); }
        catch (NumberFormatException e) { return Color.decode(fallback); }
    }

    public static Font font(int style, int size) {
        return new Font(FONT_FAMILY, style, size);
    }
    public static Font bold(int size)  { return font(Font.BOLD,  size); }
    public static Font plain(int size) { return font(Font.PLAIN, size); }
    public static Font italic(int sz)  { return font(Font.ITALIC, sz);  }
}
