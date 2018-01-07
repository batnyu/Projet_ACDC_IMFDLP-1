package Analyzer.View;

import Analyzer.Model.FileNode;
import Analyzer.Service.Filter;
import org.jdesktop.xswingx.JXSearchField;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

public class FilterPanel extends ZContainer {

    public static final Integer CHANGE_PATTERN_FILTER = 0;
    public static final Integer CHANGE_WEIGHT_FILTER = 1;
    public static final Integer CHANGE_DATE_FILTER = 2;

    ZContainer container;

    JXSearchField weightField;
    JXSearchField patternField;
    JXSearchField dateField;

    Long weight;
    String conditionWeight;

    String pattern;

    String date;

    String conditionDate;

    public FilterPanel(Dimension dim, ZContainer container) {
        super(dim);
        this.addObserver((Observer) container);
        this.container = container;
        initPanel();
    }

    public Long getWeight() {
        return weight;
    }

    public void setWeight(Long weight) {
        this.weight = weight;
    }

    public String getConditionWeight() {
        return conditionWeight;
    }

    public void setConditionWeight(String conditionWeight) {
        this.conditionWeight = conditionWeight;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getConditionDate() {
        return conditionDate;
    }

    public void setConditionDate(String conditionDate) {
        this.conditionDate = conditionDate;
    }

    public void initPanel() {

        patternField = new JXSearchField("Filter by regex pattern");
        patternField.setSearchMode(JXSearchField.SearchMode.INSTANT);
        patternField.setInstantSearchDelay(180);
        patternField.setColumns(30);

        patternField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String searchText = patternField.getText();
                if (searchText.equals("")) {
                    setPattern(".");
                } else {
                    setPattern(searchText);
                }
                //Anouncing data change
                setChanged();
                notifyObservers(CHANGE_PATTERN_FILTER);
            }
        });

        weightField = new JXSearchField("Filter by weight (Start with the condition you want [=,<,>])");
        weightField.setSearchMode(JXSearchField.SearchMode.INSTANT);
        weightField.setInstantSearchDelay(180);
        weightField.setColumns(30);

        weightField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String searchTextWithCondition = weightField.getText();
                if (searchTextWithCondition.equals("")) {
                    setWeight((long) -1);
                } else if (searchTextWithCondition.length() >= 2) {
                    setConditionWeight(searchTextWithCondition.substring(0, 1));
                    String searchText = searchTextWithCondition.substring(1, searchTextWithCondition.length());
                    setWeight(Long.parseLong(searchText));
                }
                if (conditionWeight != null) {
                    //Anouncing data change
                    setChanged();
                    notifyObservers(CHANGE_WEIGHT_FILTER);
                }

            }
        });

        dateField = new JXSearchField("Filter by date");
        dateField.setSearchMode(JXSearchField.SearchMode.REGULAR);
        dateField.setColumns(14);
        dateField.setToolTipText("Start with the condition you want [=,<,>]");

        dateField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String searchTextWithCondition = dateField.getText();
                if (searchTextWithCondition.equals("")) {
                    setDate("-1");
                } else if (searchTextWithCondition.length() >= 2) {
                    String conditionDate = searchTextWithCondition.substring(0, 1);
                    setConditionDate(conditionDate);
                    String searchText = searchTextWithCondition.substring(1, searchTextWithCondition.length());
                    setDate(searchText);
                }
                if (conditionDate != null) {
                    //Anouncing data change
                    setChanged();
                    notifyObservers(CHANGE_DATE_FILTER);
                }

            }
        });

        this.panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.panel.add(patternField);
        this.panel.add(weightField);
        this.panel.add(dateField);
    }

    public void resetFields() {
        patternField.setText("");
        weightField.setText("");
        dateField.setText("");
    }
}
