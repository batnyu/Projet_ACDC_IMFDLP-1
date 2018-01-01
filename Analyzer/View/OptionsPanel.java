package Analyzer.View;

import Analyzer.Model.FileTree;
import Analyzer.Service.Analyzer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

public class OptionsPanel extends ZContainer {

    public FilterInfo getPatternInfo() {
        return patternInfo;
    }

    public FilterInfo getWeightInfo() {
        return weightInfo;
    }

    public FilterInfo getDateInfo() {
        return dateInfo;
    }

    FilterInfo patternInfo;
    FilterInfo weightInfo;
    FilterInfo dateInfo;

    public OptionsPanel(Dimension dim) {
        super(dim);
        this.panel.setBackground(null);
        initPanel();
    }

    class FilterInfo {

        JTextField textField;
        ButtonGroup buttonGroup;

        public FilterInfo(JTextField textField, ButtonGroup buttonGroup) {
            this.textField = textField;
            this.buttonGroup = buttonGroup;
        }

        public JTextField getTextField() {
            return textField;
        }

        public ButtonGroup getButtonGroup() {
            return buttonGroup;
        }
    }

    public String getSelectedButtonText(ButtonGroup buttonGroup) {
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements(); ) {
            AbstractButton button = buttons.nextElement();

            if (button.isSelected()) {
                return button.getText();
            }
        }
        return null;
    }

    public String getPattern() {
        return this.getPatternInfo().getTextField().getText();
    }

    public long getWeight() {
        if (!this.getWeightInfo().getTextField().getText().equals("")) {
            return Long.parseLong(this.getWeightInfo().getTextField().getText());
        } else {
            return 0;
        }
    }

    public Date getDate() {
        if (!this.getDateInfo().getTextField().getText().equals("")) {
            DateFormat sourceFormat = new SimpleDateFormat("dd/MM/yyyy");
            try {
                return sourceFormat.parse(this.getDateInfo().getTextField().getText());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getSymbol(String filter) {
        if (filter.equals("weight")) {
            return getSelectedButtonText(this.getWeightInfo().getButtonGroup());
        } else {
            return getSelectedButtonText(this.getDateInfo().getButtonGroup());
        }
    }

    public void initPanel() {

        Border border = panel.getBorder();
        Border margin = new EmptyBorder(10, 10, 10, 10);
        panel.setBorder(new CompoundBorder(border, margin));

        GridBagLayout panelGridBagLayout = new GridBagLayout();
        panelGridBagLayout.columnWidths = new int[]{86, 150, 70, 0};
        panelGridBagLayout.rowHeights = new int[]{30, 30, 30, 0};
        panelGridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
        panelGridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(panelGridBagLayout);

        patternInfo = addLabelAndTextFieldAndRadioButtons("Regex pattern:", 0, panel, false);
        weightInfo = addLabelAndTextFieldAndRadioButtons("Weight:", 1, panel, true);
        dateInfo = addLabelAndTextFieldAndRadioButtons("Date (DD/MM/YYYY):", 2, panel, true);
    }

    private FilterInfo addLabelAndTextFieldAndRadioButtons(String labelText, int yPos, Container containingPanel, boolean radioButtons) {

        JLabel label = new JLabel(labelText);
        GridBagConstraints gridBagConstraintForLabel = new GridBagConstraints();
        gridBagConstraintForLabel.fill = GridBagConstraints.BOTH;
        gridBagConstraintForLabel.insets = new Insets(0, 0, 5, 5);
        gridBagConstraintForLabel.gridx = 0;
        gridBagConstraintForLabel.gridy = yPos;
        containingPanel.add(label, gridBagConstraintForLabel);

        JTextField textField = new JTextField();
        GridBagConstraints gridBagConstraintForTextField = new GridBagConstraints();
        gridBagConstraintForTextField.fill = GridBagConstraints.BOTH;
        gridBagConstraintForTextField.insets = new Insets(0, 0, 5, 5);
        gridBagConstraintForTextField.gridx = 1;
        gridBagConstraintForTextField.gridy = yPos;
        containingPanel.add(textField, gridBagConstraintForTextField);
        textField.setColumns(10);

        ButtonGroup buttons = new ButtonGroup();

        if (radioButtons) {
            JRadioButton equals = new JRadioButton("=");
            JRadioButton inferior = new JRadioButton("<");
            JRadioButton superior = new JRadioButton(">");

            buttons.add(equals);
            buttons.add(inferior);
            buttons.add(superior);
            equals.setSelected(true);

            GridBagConstraints gridBagConstraintForRadioButtons = new GridBagConstraints();
            gridBagConstraintForRadioButtons.fill = GridBagConstraints.BOTH;
            gridBagConstraintForRadioButtons.insets = new Insets(0, 0, 5, 0);
            gridBagConstraintForRadioButtons.gridx = 2;
            gridBagConstraintForRadioButtons.gridy = yPos;
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
            buttonsPanel.add(equals);
            buttonsPanel.add(inferior);
            buttonsPanel.add(superior);
            containingPanel.add(buttonsPanel, gridBagConstraintForRadioButtons);

        }

        return new FilterInfo(textField, buttons);
    }
}
