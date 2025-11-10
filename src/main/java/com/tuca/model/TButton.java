package com.tuca.model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public record TButton(JButton button) {

    public static class Builder {

        private String text = "";
        private Dimension preferredSize;
        private Dimension maximumSize;
        private Float alignmentX;
        private ActionListener actionListener;

        // Configurações de estilo
        private Color backgroundColor;
        private Color hoverColor;
        private Font font = new Font("Segoe UI", Font.BOLD, 13);
        private Color foregroundColor = Color.WHITE;
        private boolean rounded = false;
        private int borderRadius = 8;

        public Builder withText(String text) {
            this.text = text;
            return this;
        }

        public Builder withPreferredSize(int width, int height) {
            this.preferredSize = new Dimension(width, height);
            return this;
        }

        public Builder withMaximumSize(int width, int height) {
            this.maximumSize = new Dimension(width, height);
            return this;
        }

        public Builder withAlignmentX(float alignmentX) {
            this.alignmentX = alignmentX;
            return this;
        }

        public Builder withActionListener(ActionListener listener) {
            this.actionListener = listener;
            return this;
        }

        public Builder withBackgroundColor(Color color) {
            this.backgroundColor = color;
            return this;
        }

        public Builder withHoverColor(Color color) {
            this.hoverColor = color;
            return this;
        }

        public Builder withFont(String fontName, int fontStyle, int fontSize) {
            this.font = new Font(fontName, fontStyle, fontSize);
            return this;
        }

        public Builder withForegroundColor(Color color) {
            this.foregroundColor = color;
            return this;
        }

        public Builder withRoundedCorners(int radius) {
            this.rounded = true;
            this.borderRadius = radius;
            return this;
        }

        public Builder asDefaultButton(Color backgroundColor, Color hoverColor, ActionListener listener) {
            this.backgroundColor = backgroundColor;
            this.hoverColor = hoverColor;
            this.actionListener = listener;
            this.rounded = true;
            return this;
        }

        public TButton build() {
            JButton btn;

            if (rounded && backgroundColor != null) {
                final Color bgColor = backgroundColor;
                btn = new JButton(text) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(getBackground());
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), borderRadius, borderRadius);
                        g2.setColor(getForeground());
                        g2.setFont(getFont());
                        FontMetrics fm = g2.getFontMetrics();
                        int x = (getWidth() - fm.stringWidth(getText())) / 2;
                        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                        g2.drawString(getText(), x, y);
                        g2.dispose();
                    }
                };
                btn.setBackground(bgColor);
                btn.setContentAreaFilled(false);
                btn.setBorderPainted(false);
            } else {
                btn = new JButton(text);
                if (backgroundColor != null) {
                    btn.setBackground(backgroundColor);
                }
            }

            btn.setFont(font);
            btn.setForeground(foregroundColor);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (preferredSize != null) {
                btn.setPreferredSize(preferredSize);
            }
            if (maximumSize != null) {
                btn.setMaximumSize(maximumSize);
            }
            if (alignmentX != null) {
                btn.setAlignmentX(alignmentX);
            }

            if (actionListener != null) {
                btn.addActionListener(actionListener);
            }

            if (hoverColor != null && backgroundColor != null) {
                final Color originalColor = backgroundColor;
                final Color hover = hoverColor;
                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent evt) {
                        btn.setBackground(hover);
                    }

                    @Override
                    public void mouseExited(MouseEvent evt) {
                        btn.setBackground(originalColor);
                    }
                });
            }

            return new TButton(btn);
        }
    }
}