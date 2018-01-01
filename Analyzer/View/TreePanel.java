package Analyzer.View;

import Analyzer.Service.Analyzer;
import Analyzer.Service.Filter;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;

public class TreePanel extends ZContainer implements Observer {

    private boolean hash = false;
    private boolean recordInCache = false;
    private int maxDepth = Integer.MAX_VALUE;
    private boolean thread = false;

    Analyzer analyzer;
    Filter filter;

    JLabel mainLabel;
    LoadingPanel loadingPanel;
    Dimension dim;
    JTree tree;

    public TreePanel(Dimension dim, Analyzer analyzer) {
        super(dim);
        this.dim = dim;
        this.analyzer = analyzer;
        this.filter = new Filter();
        initPanel();
    }

    @Override
    public void update(Observable o, Object arg) {
        ActionsPanel actionsPanel = (ActionsPanel) o;
        Integer iMessage = (Integer) arg;
        if (iMessage == ActionsPanel.CHANGE_SELECTED_FILE) {
            getTree(actionsPanel.getCurrentSelectedFilePath());
        } else if (iMessage == ActionsPanel.CHANGE_FILTER) {
            System.out.println("pattern: " + actionsPanel.getOptionsPanel().getPattern());
            System.out.println("Weight: " + actionsPanel.getOptionsPanel().getWeightInfo().getTextField().getText());
            System.out.println("Date: " + actionsPanel.getOptionsPanel().getDateInfo().getTextField().getText());
            System.out.println("Date truc: " + actionsPanel.getOptionsPanel().getSelectedButtonText(actionsPanel.getOptionsPanel().getDateInfo().getButtonGroup()));

            if (!actionsPanel.getOptionsPanel().getPattern().equals("")) {
                filter.setPattern(actionsPanel.getOptionsPanel().getPattern());
            }

            if (actionsPanel.getOptionsPanel().getWeight() != 0) {
                switch (actionsPanel.getOptionsPanel().getSymbol("weight")) {
                    case "=":
                        filter.weightEq(actionsPanel.getOptionsPanel().getWeight());
                        break;
                    case "<":
                        filter.weightLw(actionsPanel.getOptionsPanel().getWeight());
                        break;
                    case ">":
                        filter.weightGt(actionsPanel.getOptionsPanel().getWeight());
                    default:
                        break;
                }
            }

            if (actionsPanel.getOptionsPanel().getDate() != null) {
                switch (actionsPanel.getOptionsPanel().getSymbol("date")) {
                    case "=":
                        filter.dateEq(actionsPanel.getOptionsPanel().getDate());
                        break;
                    case "<":
                        filter.dateLw(actionsPanel.getOptionsPanel().getDate());
                        break;
                    case ">":
                        filter.dateGt(actionsPanel.getOptionsPanel().getDate());
                    default:
                        break;
                }
            }

            //Reset filter when no fields completed
            if(actionsPanel.getOptionsPanel().getPattern().equals("") && actionsPanel.getOptionsPanel().getWeight() == 0 && actionsPanel.getOptionsPanel().getDate() == null){
                System.out.println("RESET FILTER");
                filter = new Filter();
            }
        }

    }

    public void initPanel() {

        ActionsPanel actionsPanel = new ActionsPanel(this.dim, this);
        this.panel.add(actionsPanel.getPanel(), BorderLayout.NORTH);

        mainLabel = new JLabel("Choose a directory");
        mainLabel.setVerticalAlignment(JLabel.CENTER);
        mainLabel.setHorizontalAlignment(JLabel.CENTER);
        this.panel.add(mainLabel, BorderLayout.CENTER);
    }

    public void getTree(String path) {

        //Reset view
        if (tree != null) {
            this.panel.remove(tree);
        }
        if (mainLabel != null) {
            this.panel.remove(mainLabel);
        }

        loadingPanel = new LoadingPanel(this.dim, "Building the tree rooted by " + path);
        this.panel.add(loadingPanel.getPanel(), BorderLayout.CENTER);
        this.panel.revalidate();

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {

                analyzer.buildFileTree(path,filter, thread, hash, recordInCache, maxDepth);
                return null;
            }

            @Override
            public void done() {
                displayTree();
            }
        };

        // Call the SwingWorker from within the Swing thread
        worker.execute();
    }

    public void displayTree() {

        this.panel.remove(this.loadingPanel.getPanel());
        this.panel.revalidate();
        this.panel.repaint();

        //JTreeTable tree = new JTreeTable((TreeTableModel)analyzer.getTreeModel());
        //this.panel.add(tree, BorderLayout.CENTER);

        tree = new JTree(analyzer.getRoot());
        this.panel.add(tree, BorderLayout.LINE_START);
    }

}
