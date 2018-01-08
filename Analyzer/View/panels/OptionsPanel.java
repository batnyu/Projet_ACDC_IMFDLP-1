package Analyzer.View.panels;

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

    private OptionsInfo hashInfo;
    private OptionsInfo threadInfo;
    private OptionsInfo cacheInfo;
    private OptionsInfo depthInfo;
    private OptionsInfo patternInfo;
    private OptionsInfo weightInfo;
    private OptionsInfo dateInfo;

    private ZContainer container;

    private int posTreePanel[] = {4,5,6};
    private int posDuplicatesPanel[] = {0,1,2};

    public OptionsPanel(Dimension dim, ZContainer container) {
        super(dim);
        this.container = container;
        this.panel.setBackground(null);
        initPanel();
    }

    public OptionsInfo getHashInfo() {
        return hashInfo;
    }

    public OptionsInfo getThreadInfo() {
        return threadInfo;
    }

    public OptionsInfo getCacheInfo() {
        return cacheInfo;
    }

    public OptionsInfo getDepthInfo() {
        return depthInfo;
    }

    public OptionsInfo getPatternInfo() {
        return patternInfo;
    }

    public OptionsInfo getWeightInfo() {
        return weightInfo;
    }

    public OptionsInfo getDateInfo() {
        return dateInfo;
    }

    class OptionsInfo {

        JTextField textField;
        ButtonGroup buttonGroup;

        public OptionsInfo(JTextField textField, ButtonGroup buttonGroup) {
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

    public String getDepth() {
        return this.getDepthInfo().getTextField().getText();
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

        String result;

        switch (filter) {
            case "weight":
                result = getSelectedButtonText(this.getWeightInfo().getButtonGroup());
                break;
            case "date":
                result = getSelectedButtonText(this.getDateInfo().getButtonGroup());
                break;
            default:
                result = "";
                break;
        }

        return result;
    }

    public boolean getBoolean(String filter) {
        boolean result;
        switch (filter) {
            case "cache":
                result = stringToBoolean(getSelectedButtonText(this.getCacheInfo().getButtonGroup()));
                break;
            case "thread":
                result = stringToBoolean(getSelectedButtonText(this.getThreadInfo().getButtonGroup()));
                break;
            case "hash":
                result = stringToBoolean(getSelectedButtonText(this.getHashInfo().getButtonGroup()));
                break;
            default:
                result = false;
                break;
        }
        return result;
    }

    public boolean stringToBoolean(String str) {
        if (str.equals("true")) {
            return true;
        } else {
            return false;
        }
    }

    public void initPanel() {

        Border border = panel.getBorder();
        Border margin = new EmptyBorder(10, 10, 10, 10);
        panel.setBorder(new CompoundBorder(border, margin));

        GridBagLayout panelGridBagLayout = new GridBagLayout();
        panelGridBagLayout.columnWidths = new int[]{86, 86, 150, 70, 0};
        panelGridBagLayout.rowHeights = new int[]{30, 30, 30, 30, 0};
        panelGridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
        panelGridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(panelGridBagLayout);

        int pos[] = posDuplicatesPanel.clone();

        if (this.container instanceof TreePanel) {
            hashInfo = addLabelAndTextFieldAndRadioButtons("Calculate hash:", 0, panel, "boolean");
            threadInfo = addLabelAndTextFieldAndRadioButtons("Multi-thread:", 1, panel, "boolean");
            cacheInfo = addLabelAndTextFieldAndRadioButtons("Record cache:", 2, panel, "boolean");
            depthInfo = addLabelAndTextFieldAndRadioButtons("Max depth:", 3, panel, "no");
            pos  = posTreePanel.clone();

        }
        patternInfo = addLabelAndTextFieldAndRadioButtons("Regex pattern:", pos[0], panel, "condition");
        weightInfo = addLabelAndTextFieldAndRadioButtons("Weight:", pos[1], panel, "condition");
        dateInfo = addLabelAndTextFieldAndRadioButtons("Date (DD/MM/YYYY):", pos[2], panel, "condition");
    }

    private OptionsInfo addLabelAndTextFieldAndRadioButtons(String labelText, int yPos, Container containingPanel, String radioButtons) {

        JLabel label = new JLabel(labelText);
        GridBagConstraints gridBagConstraintForLabel = new GridBagConstraints();
        gridBagConstraintForLabel.fill = GridBagConstraints.BOTH;
        gridBagConstraintForLabel.insets = new Insets(0, 0, 5, 5);
        gridBagConstraintForLabel.gridx = 0;
        gridBagConstraintForLabel.gridy = yPos;
        containingPanel.add(label, gridBagConstraintForLabel);

        JTextField textField = null;
        if (!radioButtons.equals("boolean")) {
            textField = new JTextField();
            GridBagConstraints gridBagConstraintForTextField = new GridBagConstraints();
            gridBagConstraintForTextField.fill = GridBagConstraints.BOTH;
            gridBagConstraintForTextField.insets = new Insets(0, 0, 5, 5);
            gridBagConstraintForTextField.gridx = 1;
            gridBagConstraintForTextField.gridy = yPos;
            containingPanel.add(textField, gridBagConstraintForTextField);
            textField.setColumns(10);
        }

        ButtonGroup buttons = new ButtonGroup();

        if (radioButtons.equals("condition")) {
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
        } else if (radioButtons.equals("boolean")) {
            JRadioButton trueOption = new JRadioButton("true");
            JRadioButton falseOption = new JRadioButton("false");

            buttons.add(trueOption);
            buttons.add(falseOption);
            falseOption.setSelected(true);

            GridBagConstraints gridBagConstraintForRadioButtons = new GridBagConstraints();
            gridBagConstraintForRadioButtons.fill = GridBagConstraints.BOTH;
            gridBagConstraintForRadioButtons.insets = new Insets(0, 0, 5, 0);
            gridBagConstraintForRadioButtons.gridx = 1;
            gridBagConstraintForRadioButtons.gridy = yPos;
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
            buttonsPanel.add(trueOption);
            buttonsPanel.add(falseOption);
            containingPanel.add(buttonsPanel, gridBagConstraintForRadioButtons);
        }

        return new OptionsInfo(textField, buttons);
    }
}
