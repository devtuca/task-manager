package com.tuca.model;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public record TLabel(JLabel jLabel) {

    public static class Builder {

        private String text = "";
        private Font font = new Font("Segoe UI", Font.PLAIN, 12);
        private Color backgroundC = Color.BLACK;
        private Color foregroundC = Color.WHITE;
        private boolean opaque = false;
        private Border border = null;

        public Builder withText(String text) {
            this.text = text;
            return this;
        }

        public Builder withFont(String fontName, int fontStyle, int fontSize) {
            this.font = new Font(fontName, fontStyle, fontSize);
            return this;
        }

        public Builder withForeground(Color color) {
            this.foregroundC = color;
            return this;
        }

        public Builder withBackground(Color color) {
            this.backgroundC = color;
            return this;
        }

        public Builder withOpaque(boolean opaque) {
            this.opaque = opaque;
            return this;
        }

        public Builder withBorder(int top, int left, int bottom, int right) {
            this.border = new EmptyBorder(top, left, bottom, right);
            return this;
        }

        public TLabel build() {
            JLabel label = new JLabel(text);
            label.setFont(font);
            label.setBackground(backgroundC);
            label.setForeground(foregroundC);
            label.setOpaque(opaque);
            label.setBorder(border);
            return new TLabel(label);
        }
    }
}
