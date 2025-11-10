package com.tuca.model;

import lombok.Getter;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class TScrollPane {

    @Getter
    private JScrollPane scrollPane;

    private TScrollPane(JScrollPane jScrollPane) {
        this.scrollPane = jScrollPane;
    }

    @Getter
    public static class Builder {


        private JScrollPane scrollPane;
        private Border border;
        private Color background;
        private Color viewPortBG;
        private int unitIncrement;

        public Builder withBorder() {
            this.border = null;
            return this;
        }

        public Builder withBackground(Color backgroundC) {
            this.background = backgroundC;
            return this;
        }

        public Builder withViewPortBG(Color viewPortBG) {
            this.viewPortBG = viewPortBG;
            return this;
        }

        public Builder withUnitIncrement(int unitIncrement) {
            this.unitIncrement = unitIncrement;
            return this;
        }

        public TScrollPane build(JPanel panel) {

            this.scrollPane = new JScrollPane(panel);
            scrollPane.setBorder(this.border);
            scrollPane.setBackground(this.background);
            scrollPane.getViewport().setBackground(this.viewPortBG);
            scrollPane.getVerticalScrollBar().setUnitIncrement(this.unitIncrement);

            return new TScrollPane(scrollPane);
        }
    }

}
