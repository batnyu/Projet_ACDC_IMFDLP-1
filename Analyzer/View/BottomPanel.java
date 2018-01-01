package Analyzer.View;

import Analyzer.Control.ErrorHandler;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class BottomPanel extends ZContainer implements ErrorHandler {

    Dimension size;
    ArrayList<String> errors;

    JLabel errorsLabel;

    public BottomPanel(Dimension size) {
        super(size);
        this.size = size;
        errors = new ArrayList<String>();
        initPanel();
    }

    protected void initPanel() {
        errorsLabel = new JLabel("Errors : " + this.errors.size());
        errorsLabel.setHorizontalAlignment(JLabel.LEFT);
        errorsLabel.setFont(arialPlain);
        this.panel.setBackground(Color.LIGHT_GRAY);
        this.panel.setPreferredSize(new Dimension(((int) this.size.getWidth()), 27));
        this.panel.add(errorsLabel);
    }

    @Override
    public void capturedError(Exception exception) {
        System.out.println("Message reçu:" + exception.getMessage());
        this.errors.add(exception.getMessage());
        errorsLabel.setText("Errors : " + this.errors.size());
    }
}
