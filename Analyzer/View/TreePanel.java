package Analyzer.View;

import Analyzer.Model.FileNode;
import Analyzer.Service.Analyzer;
import Analyzer.Service.Filter;
import Analyzer.Model.FileRowModel;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import org.netbeans.swing.outline.RenderDataProvider;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Observable;
import java.util.Observer;

public class TreePanel extends ZContainer implements Observer {

    private boolean hash = false;
    private boolean recordInCache = false;
    private int maxDepth = Integer.MAX_VALUE;
    private boolean thread = false;

    Analyzer analyzer;
    Filter filter;

    JPanel mainPart;
    JLabel mainLabel;
    LoadingPanel loadingPanel;
    Dimension dim;
    JTree tree;
    JScrollPane jScrollPane;
    Outline outline;

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
            if (actionsPanel.getOptionsPanel().getPattern().equals("") && actionsPanel.getOptionsPanel().getWeight() == 0 && actionsPanel.getOptionsPanel().getDate() == null) {
                System.out.println("RESET FILTER");
                filter = new Filter();
            }
        }

    }

    public void initPanel() {

        ActionsPanel actionsPanel = new ActionsPanel(this.dim, this);
        this.panel.add(actionsPanel.getPanel(), BorderLayout.NORTH);

        mainPart = new JPanel(new BorderLayout());

        final JTextField field = new JTextField(10);
        field.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent ke) {
                super.keyTyped(ke);
                if (field.getText().equals("")) {
                    outline.unsetQuickFilter();
                } else {
                    outline.setQuickFilter(0, field.getText());
                }
            }
        });


        mainPart.add(field, BorderLayout.NORTH);

        mainLabel = new JLabel("Choose a directory");
        mainLabel.setVerticalAlignment(JLabel.CENTER);
        mainLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPart.add(mainLabel, BorderLayout.CENTER);


        this.panel.add(mainPart, BorderLayout.CENTER);


    }

    public void getTree(String path) {

        //Reset view
        if (jScrollPane != null) {
            mainPart.remove(jScrollPane);
        }
        if (mainLabel != null) {
            mainPart.remove(mainLabel);
        }

        loadingPanel = new LoadingPanel(this.dim, "Building the tree rooted by " + path);
        mainPart.add(loadingPanel.getPanel(), BorderLayout.CENTER);
        mainPart.revalidate();

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {

                analyzer.buildFileTree(path, filter, thread, hash, recordInCache, maxDepth);
                return null;
            }

            @Override
            public void done() {
                analyzer.setWeight(analyzer.getRoot());
                displayTree();
            }
        };

        // Call the SwingWorker from within the Swing thread
        worker.execute();
    }

    public void displayTree() {

//        this.panel.remove(this.loadingPanel.getPanel());
//        this.panel.revalidate();
//        this.panel.repaint();

        mainPart.remove(this.loadingPanel.getPanel());
        mainPart.revalidate();
        mainPart.repaint();

        //FilteredTree ftree = new FilteredTree(analyzer.getRoot());

        TreeModel treeModel = analyzer.getTreeModel();

        OutlineModel mdl = DefaultOutlineModel.createOutlineModel(treeModel,
                new FileRowModel(), true, "File System");

        outline = new Outline();
        outline.setRenderDataProvider(new FileDataProvider());
        outline.setRootVisible(true);
        outline.setModel(mdl);

        jScrollPane = new JScrollPane(outline);
        mainPart.add(jScrollPane, BorderLayout.CENTER);

//        final JTree tree = ftree.getTree();
//
//        MouseListener ml = new MouseAdapter() {
//            public void mousePressed(MouseEvent e) {
//                int selRow = tree.getRowForLocation(e.getX(), e.getY());
//                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
//                if (selRow != -1) {
//                    if (e.getClickCount() == 1) {
//                        System.out.println("" + selPath.getLastPathComponent());
//                    }
//                }
//            }
//        };
//        tree.addMouseListener(ml);
    }

    private class FileDataProvider implements RenderDataProvider {
        public java.awt.Color getBackground(Object o) {
            return null;
        }

        public String getDisplayName(Object o) {
            DefaultMutableTreeNode node = ((DefaultMutableTreeNode) o);
            FileNode fileNode = ((FileNode) node.getUserObject());

            return fileNode.getName();
        }

        public java.awt.Color getForeground(Object o) {
            DefaultMutableTreeNode node = ((DefaultMutableTreeNode) o);
            FileNode fileNode = ((FileNode) node.getUserObject());

            if (!fileNode.isDirectory() && !fileNode.canWrite()) {
                return UIManager.getColor("controlShadow");
            }
            return null;
        }

        public javax.swing.Icon getIcon(Object o) {
            return null;
        }

        public String getTooltipText(Object o) {
            DefaultMutableTreeNode node = ((DefaultMutableTreeNode) o);
            FileNode fileNode = ((FileNode) node.getUserObject());
            return fileNode.getAbsolutePath();
        }

        public boolean isHtmlDisplayName(Object o) {
            return false;
        }
    }

}
