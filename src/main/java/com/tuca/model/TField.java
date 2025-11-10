package com.tuca.model;

import lombok.Getter;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TField {

    @Getter
    private String text;

    public TField(String text) {
        this.text = text;
    }

    @Getter
    public static class Builder {

        private JTextField jTextField;
        private String text = "Indefinido";
        private int columns = 30;
        private Font font = new Font("Segoe UI", Font.PLAIN, 14);
        private Color foregroundColor = new Color(255, 255, 255);
        private Color backgroundColor = new Color(255, 255, 255);
        private Border border;

        public Builder withColumns(int amount) {
            this.columns = amount;
            return this;
        }

        public Builder withFont(String fontName, int fontType, int fontSize) {
            this.font = new Font(fontName, fontType, fontSize);
            return this;
        }

        public Builder withForeground(Color color) {
            this.foregroundColor = color;
            return this;
        }

        public Builder withBackground(Color color) {
            this.backgroundColor = color;
            return this;
        }

        public Builder withBorder(Color color, int thickness, boolean rounded, int top, int left, int bottom, int right) {
            this.border = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(color, thickness, rounded), new EmptyBorder(top, left, bottom, right));
            return this;
        }

        public Builder withText(String text) {
            this.text = text;
            return this;
        }

        public JTextField build() {
            this.jTextField = new JTextField(columns);
            jTextField.setFont(font);
            jTextField.setForeground(foregroundColor);
            jTextField.setBackground(backgroundColor);
            jTextField.setBorder(border);
            jTextField.setText(text);
            return jTextField;
        }
    }
}
