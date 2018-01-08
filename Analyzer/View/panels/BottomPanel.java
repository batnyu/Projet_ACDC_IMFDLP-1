package Analyzer.View.panels;

import Analyzer.Control.ErrorHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Map;

public class BottomPanel extends ZContainer implements ErrorHandler {

    private Dimension size;

    private ArrayList<String> errors;

    private JLabel errorsLabel;
    private ErrorsPanel errorsPanel;

    public BottomPanel(Dimension size) {
        super(size);
        this.size = size;
        this.errors = new ArrayList<String>();
        initPanel();
    }

    public ArrayList<String> getErrors() {
        return errors;
    }

    public void initPanel() {
        errorsLabel = new JLabel("Errors : " + this.errors.size());
        errorsLabel.setHorizontalAlignment(JLabel.LEFT);

        errorsLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        errorsLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 0) {
                    errorsPanel = new ErrorsPanel(null, getErrors());
                    JOptionPane.showConfirmDialog(null, errorsPanel.getPanel(),
                            "Errors", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
                }
            }

            Font original;

            @Override
            public void mouseEntered(MouseEvent e) {
                original = e.getComponent().getFont();
                Map attributes = original.getAttributes();
                attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                e.getComponent().setFont(original.deriveFont(attributes));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                e.getComponent().setFont(original);
            }
        });

        this.panel.setBackground(new Color(240,240,240));
        this.panel.setPreferredSize(new Dimension(((int) this.size.getWidth()), 27));
        this.panel.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
        this.panel.add(errorsLabel);

    }

    @Override
    public void capturedError(Exception exception) {
        System.out.println("Message reçu:" + exception.toString());
        this.errors.add(exception.toString());
        errorsLabel.setText("Errors : " + this.errors.size());
    }


}
