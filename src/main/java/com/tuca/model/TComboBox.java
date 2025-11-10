package com.tuca.model;

import lombok.Data;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TComboBox {

    private TComboBox() {
    }

    @Data
    public static class Builder {

        private JComboBox<String> jComboBox;
        private String[] options;
        private Font font;
        private Dimension dimension;
        private Color background;
        private Color foregrond;
        private Border border;

        public Builder withFont(String fontName, int fontType, int fontSize) {
            this.font = new Font(fontName, fontType, fontSize);
            return this;
        }

        public Builder withPreferredSize(int dimW, int dimH) {
            this.dimension = new Dimension(dimW, dimH);
            return this;
        }

        public Builder withBackground(Color backgroundC) {
            this.background = backgroundC;
            return this;
        }

        public Builder withForeground(Color foregroundC) {
            this.foregrond = foregroundC;
            return this;
        }

        public Builder withOptions(String[] options) {
            this.options = options;
            return this;
        }

        public Builder withBorder(Color color, int thickness, boolean rounded, int top, int left, int bottom, int right) {
            this.border = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(color, thickness, rounded), new
                    EmptyBorder(top, left, bottom, right));
            return this;
        }

        public JComboBox<String> build() {
            this.jComboBox = new JComboBox<>(this.options);
            jComboBox.setFont(font);
            jComboBox.setPreferredSize(dimension);
            jComboBox.setBackground(background);
            jComboBox.setForeground(foregrond);
            jComboBox.setBorder(border);
            return jComboBox;
        }
    }

}
