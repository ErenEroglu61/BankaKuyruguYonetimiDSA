package com.bankqueue.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * ┌─────────────────────────────────────────────┐
 *   UI YARDIMCI KATMANI
 *   Tüm Swing bileşen fabrika metodları burada.
 *   Hiçbir iş mantığı içermez — sadece görünüm.
 * └─────────────────────────────────────────────┘
 */
public final class UIHelper {

    private UIHelper() {}   // Instantiate edilemez

    public static JLabel label(String text, Color color, int size, int style) {
        JLabel l = new JLabel(text);
        l.setForeground(color);
        l.setFont(Theme.font(style, size));
        return l;
    }

    public static JLabel label(String text, Color color, int size) {
        return label(text, color, size, Font.PLAIN);
    }

    public static JButton button(String text, Color bg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        applyButtonStyle(b, bg);
        return b;
    }

    private static void applyButtonStyle(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(Theme.bold(12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            final Color base = bg;
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(base.brighter()); }
            @Override public void mouseExited (MouseEvent e) { b.setBackground(base); }
        });
    }

    public static JTextField textField() {
        JTextField tf = new JTextField();
        tf.setBackground(Theme.SURF2);
        tf.setForeground(Theme.FG);
        tf.setCaretColor(Theme.PRIMARY);
        tf.setFont(Theme.plain(13));
        tf.setBorder(BorderFactory.createCompoundBorder(
            roundBorder(6, Theme.BORDER), padding(6, 10)));
        return tf;
    }

    public static JSpinner spinner(int val, int min, int max, int step) {
        JSpinner sp = new JSpinner(new SpinnerNumberModel(val, min, max, step));
        sp.setBackground(Theme.SURFACE);
        sp.setForeground(Theme.FG);
        sp.setFont(Theme.plain(12));
        JTextField editor = ((JSpinner.DefaultEditor) sp.getEditor()).getTextField();
        editor.setBackground(Theme.SURF2);
        editor.setForeground(Theme.FG);
        editor.setFont(Theme.plain(12));
        return sp;
    }

    public static JPanel panel(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(Theme.BG);
        return p;
    }

    public static JPanel surfacePanel(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(Theme.SURFACE);
        return p;
    }

    public static JScrollPane scroll(Component view) {
        JScrollPane sc = new JScrollPane(view);
        sc.setBackground(Theme.BG);
        sc.getViewport().setBackground(Theme.BG);
        sc.setBorder(null);
        sc.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        styleScrollBar(sc.getVerticalScrollBar());
        return sc;
    }

    private static void styleScrollBar(JScrollBar bar) {
        bar.setBackground(Theme.BG);
        bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor     = Theme.BORDER;
                trackColor     = Theme.SURFACE;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            private JButton zeroBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }

    public static Border roundBorder(int arc, Color color) {
        return new RoundBorder(arc, color);
    }

    public static Border padding(int v, int h) {
        return BorderFactory.createEmptyBorder(v, h, v, h);
    }

    public static TitledBorder titledBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            title, TitledBorder.LEFT, TitledBorder.TOP,
            Theme.bold(11), Theme.FG2);
    }

    public static Component vgap(int h) {
        return Box.createRigidArea(new Dimension(0, h));
    }

    public static Component hgap(int w) {
        return Box.createRigidArea(new Dimension(w, 0));
    }

    public static class RoundBorder extends AbstractBorder {
        private final int arc;
        private final Color color;

        public RoundBorder(int arc, Color color) {
            this.arc   = arc;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w - 1, h - 1, arc, arc);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(arc / 4, arc / 4, arc / 4, arc / 4);
        }
    }
}
