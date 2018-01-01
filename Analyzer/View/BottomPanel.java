package Analyzer.View;

import Analyzer.Control.ErrorHandler;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class BottomPanel extends ZContainer implements ErrorHandler {

    Dimension size;
    ArrayList<String> errors;

    public BottomPanel(Dimension size) {
        super(size);
        this.size = size;
        errors = new ArrayList<String>();
        initPanel();
    }

    protected void initPanel() {
        JLabel errors = new JLabel("Errors : " + this.errors.size());
        errors.setHorizontalAlignment(JLabel.LEFT);
        errors.setFont(arialPlain);
        this.panel.setBackground(Color.LIGHT_GRAY);
        this.panel.setPreferredSize(new Dimension(((int) this.size.getWidth()), 27));
        this.panel.add(errors);
    }

    @Override
    public void capturedError(Exception exception) {
        System.out.println("Message reçu:" + exception.getMessage());
        this.errors.add(exception.getMessage());
        this.panel.revalidate();
    }
}
