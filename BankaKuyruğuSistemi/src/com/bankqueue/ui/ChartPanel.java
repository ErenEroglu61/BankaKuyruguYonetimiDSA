package com.bankqueue.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;

/**
 * ┌─────────────────────────────────────────────┐
 *   GRAFİK BİLEŞENİ
 *   Harici kütüphane yok — saf Swing ile çizim.
 *   Çizgi + bar hibrit grafik, gradient dolgu.
 * └─────────────────────────────────────────────┘
 */
public class ChartPanel extends JPanel {

    private final String        label;
    private final List<Integer> data;
    private final Color         lineColor;

    private static final int PAD_L = 48;
    private static final int PAD_R = 18;
    private static final int PAD_T = 22;
    private static final int PAD_B = 32;

    public ChartPanel(String label, List<Integer> data, Color lineColor) {
        this.label     = label;
        this.data      = data;
        this.lineColor = lineColor;
        setBackground(Theme.SURFACE);
        setPreferredSize(new Dimension(400, 170));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int W = getWidth(), H = getHeight();
        int cW = W - PAD_L - PAD_R;
        int cH = H - PAD_T - PAD_B;

        // Arka plan yuvarlak
        g2.setColor(Theme.SURFACE);
        g2.fillRoundRect(0, 0, W, H, 12, 12);

        int maxVal = data.isEmpty() ? 10 : Math.max(1, data.stream().mapToInt(v->v).max().orElse(10));

        g2.setFont(Theme.plain(9));
        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            int y = PAD_T + cH - (int)((double)i / gridLines * cH);
            g2.setColor(Theme.BORDER);
            g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                                         0, new float[]{3,4}, 0));
            g2.drawLine(PAD_L, y, PAD_L + cW, y);
            g2.setColor(Theme.FG2);
            g2.setStroke(new BasicStroke(1f));
            g2.drawString(String.valueOf((int)((double)i/gridLines*maxVal)), 4, y + 4);
        }

        // Veri yok mesaj
        if (data.isEmpty()) {
            g2.setColor(Theme.FG2);
            g2.setFont(Theme.italic(11));
            String msg = "Simülasyonu başlatın…";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, PAD_L + (cW - fm.stringWidth(msg))/2, PAD_T + cH/2);
            drawAxes(g2, cW, cH);
            g2.dispose();
            return;
        }

        int n    = data.size();
        float step = (float) cW / n;
        float barW = Math.max(2f, step * 0.55f);

        // Gradient dolgu alanı
        GradientPaint gp = new GradientPaint(
            0, PAD_T,    new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 55),
            0, PAD_T+cH, new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 4)
        );
        if (n > 1) {
            int[] px = new int[n + 2], py = new int[n + 2];
            for (int i = 0; i < n; i++) {
                px[i] = PAD_L + (int)(i * step + step/2);
                py[i] = PAD_T + cH - (int)((double) data.get(i) / maxVal * cH);
            }
            px[n] = PAD_L + cW; py[n] = PAD_T + cH;
            px[n+1] = PAD_L;    py[n+1] = PAD_T + cH;
            g2.setPaint(gp);
            g2.fillPolygon(px, py, n + 2);
        }

        // Barlar
        g2.setStroke(new BasicStroke(1f));
        for (int i = 0; i < n; i++) {
            int x   = PAD_L + (int)(i * step + step/2 - barW/2);
            int bH  = (int)((double) data.get(i) / maxVal * cH);
            int y   = PAD_T + cH - bH;
            g2.setColor(new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 100));
            g2.fillRoundRect(x, y, (int)barW, bH, 3, 3);
        }

        // Çizgi
        g2.setColor(lineColor);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 1; i < n; i++) {
            int x1 = PAD_L + (int)((i-1)*step + step/2);
            int y1 = PAD_T + cH - (int)((double) data.get(i-1) / maxVal * cH);
            int x2 = PAD_L + (int)(i    *step + step/2);
            int y2 = PAD_T + cH - (int)((double) data.get(i)   / maxVal * cH);
            g2.drawLine(x1, y1, x2, y2);
        }

        // Son nokta
        int lx = PAD_L + (int)((n-1)*step + step/2);
        int ly = PAD_T + cH - (int)((double) data.get(n-1) / maxVal * cH);
        g2.setColor(Color.WHITE);
        g2.fillOval(lx-4, ly-4, 8, 8);
        g2.setColor(lineColor);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(lx-4, ly-4, 8, 8);

        // Son değer etiketi
        g2.setColor(Theme.FG);
        g2.setFont(Theme.bold(10));
        g2.drawString(String.valueOf(data.get(n-1)), lx + 7, ly - 2);

        // Alt istatistik satırı
        int avg = (int) data.stream().mapToInt(v->v).average().orElse(0);
        int mx  = data.stream().mapToInt(v->v).max().orElse(0);
        g2.setColor(Theme.FG2);
        g2.setFont(Theme.plain(10));
        g2.drawString("Ort: " + avg + "  |  Max: " + mx + "  |  n=" + n, PAD_L + 4, H - 6);

        drawAxes(g2, cW, cH);
        g2.dispose();
    }

    private void drawAxes(Graphics2D g2, int cW, int cH) {
        g2.setColor(Theme.BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(PAD_L, PAD_T, PAD_L, PAD_T + cH);
        g2.drawLine(PAD_L, PAD_T + cH, PAD_L + cW, PAD_T + cH);
        g2.setColor(Theme.FG2);
        g2.setFont(Theme.bold(11));
        g2.drawString(label, PAD_L + 4, PAD_T - 6);
    }
}
