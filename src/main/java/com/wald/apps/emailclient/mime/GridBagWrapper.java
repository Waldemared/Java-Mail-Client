package com.wald.apps.emailclient.mime;

import javax.swing.*;
import java.awt.*;

public class GridBagWrapper {
    // координаты текущей ячейки
    private int gridx, gridy;
    // настраиваемый объект GridBagConstraints
    private GridBagConstraints constraints;

    // возвращает настроенный объект GridBagConstraints
    public GridBagConstraints get() {
        return constraints;
    }
    // двигается на следующую ячейку
    public GridBagWrapper nextCell() {
        constraints = new GridBagConstraints();
        constraints.gridx = gridx++;
        constraints.gridy = gridy;
        // для удобства возвращаем себя
        return this;
    }
    // двигается на следующий ряд
    public GridBagWrapper nextRow() {
        gridy++;
        gridx = 0;
        constraints.gridx = 0;
        constraints.gridy = gridy;
        return this;
    }
    // раздвигает ячейку до конца строки
    public GridBagWrapper span() {
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        return this;
    }
    // заполняет ячейку по горизонтали
    public GridBagWrapper fillHorizontally() {
        constraints.fill = GridBagConstraints.HORIZONTAL;
        return this;
    }
    // вставляет распорку справа
    public GridBagWrapper gap(int size) {
        constraints.insets.right = size;
        return this;
    }

    public GridBagWrapper spanY() {
        constraints.gridheight = GridBagConstraints.REMAINDER;
        return this;
    }


    public GridBagWrapper fillBoth() {
        constraints.fill = GridBagConstraints.BOTH;
        return this;
    }

    public GridBagWrapper alignLeft() {
        constraints.anchor = GridBagConstraints.LINE_START;
        return this;
    }

    public GridBagWrapper alignRight() {
        constraints.anchor = GridBagConstraints.LINE_END;
        return this;
    }

    public GridBagWrapper setInsets(int left, int top, int right, int bottom) {
        Insets i = new Insets(top, left, bottom, right);
        constraints.insets = i;
        return this;
    }

    public GridBagWrapper setWeights(float horizontal, float vertical) {
        constraints.weightx = horizontal;
        constraints.weighty = vertical;
        return this;
    }

    public void insertEmptyRow(Container c, int height) {
        Component comp = Box.createVerticalStrut(height);
        nextCell().nextRow().fillHorizontally().span();
        c.add(comp, get());
        nextRow();
    }

    public void insertEmptyFiller(Container c) {
        Component comp = Box.createGlue();
        nextCell().nextRow().fillBoth().span().spanY().setWeights(1.0f, 1.0f);
        c.add(comp, get());
        nextRow();
    }
}
