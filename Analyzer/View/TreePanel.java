package Analyzer.View;

import Analyzer.Model.FileNode;
import Analyzer.Service.Analyzer;
import Analyzer.Service.Filter;
import Analyzer.Model.FileRowModel;
import org.jdesktop.xswingx.JXSearchField;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import org.netbeans.swing.outline.RenderDataProvider;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

public class TreePanel extends ZContainer implements Observer {

    private boolean hash = false;
    private boolean recordInCache = false;
    private int maxDepth = Integer.MAX_VALUE;
    private boolean thread = false;

    Analyzer analyzer;
    Filter filter;
    Filter filterAfter;

    JPanel mainPart;
    JLabel mainLabel;
    LoadingPanel loadingPanel;
    Dimension dim;
    JTree tree;
    JScrollPane jScrollPane;
    Outline outline;
    FilterPanel filterPanel;

    SwingWorker<Void, Void> getTreeWorker;

    public TreePanel(Dimension dim, Analyzer analyzer) {
        super(dim);
        this.dim = dim;
        this.analyzer = analyzer;
        this.filter = new Filter();
        initPanel();
    }

    @Override
    public void update(Observable o, Object arg) {

        if (o instanceof ActionsPanel) {
            ActionsPanel actionsPanel = (ActionsPanel) o;
            Integer iMessage = (Integer) arg;
            if (iMessage == ActionsPanel.CHANGE_SELECTED_FILE) {
                this.filterAfter = new Filter();
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
        } else if (o instanceof FilterPanel) {
            FilterPanel filterPanel = (FilterPanel) o;
            Integer iMessage = (Integer) arg;

            if (iMessage.equals(FilterPanel.CHANGE_PATTERN_FILTER)) {
                String pattern = filterPanel.getPattern();
                filterAfter.setPattern(pattern);
//                for (int i = 0; i < tree.getRowCount(); i++) {
//                    tree.expandRow(i);
//                }
                for (int i = 0; i < outline.getRowCount(); i++) {
                    TreePath path = outline.getClosestPathForLocation(i, 0);
                    System.out.println(path.toString());
                    outline.expandPath(path);
                }
                outline.setQuickFilter(FileRowModel.FILE_SYSTEM_COLUMN, filterAfter);
            } else if (iMessage.equals(FilterPanel.CHANGE_WEIGHT_FILTER)) {
                Long weight = filterPanel.getWeight();
                String condition = filterPanel.getConditionWeight();

                switch (condition) {
                    case "=":
                        System.out.println("EQUALS");
                        filterAfter.weightEq(weight);
                        break;
                    case "<":
                        System.out.println("LOWER");
                        filterAfter.weightLw(weight);
                        break;
                    case ">":
                        System.out.println("GREATER");
                        filterAfter.weightGt(weight);
                        break;
                    default:
                        filterAfter.weightEq(weight);
                        break;
                }
                outline.setQuickFilter(FileRowModel.WEIGHT_COLUMN, filterAfter);

            } else if (iMessage.equals(FilterPanel.CHANGE_DATE_FILTER)) {
                Date date = null;
                String condition = filterPanel.getConditionDate();

                DateFormat sourceFormat = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    date = sourceFormat.parse(filterPanel.getDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                switch (condition) {
                    case "=":
                        filterAfter.dateEq(date);
                        break;
                    case "<":
                        System.out.println("LOWER");
                        filterAfter.dateLw(date);
                        break;
                    case ">":
                        System.out.println("GREATER");
                        filterAfter.dateGt(date);
                        break;
                    default:
                        filterAfter.dateEq(date);
                        break;
                }
                outline.setQuickFilter(FileRowModel.DATE_COLUMN, filterAfter);
            }
        }

    }

    public void initPanel() {
        filterPanel = new FilterPanel(this.dim, this);
        ActionsPanel actionsPanel = new ActionsPanel(this.dim, this);
        this.panel.add(actionsPanel.getPanel(), BorderLayout.NORTH);

        mainPart = new JPanel(new BorderLayout());

        mainLabel = new JLabel("Choose a directory");
        mainLabel.setVerticalAlignment(JLabel.CENTER);
        mainLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPart.add(mainLabel, BorderLayout.CENTER);


        this.panel.add(mainPart, BorderLayout.CENTER);


    }

    public void getTree(String path) {

        //Reset textfields
        filterPanel.resetFields();

        if (getTreeWorker != null) {
            getTreeWorker.cancel(true);
            getTreeWorker = null;
        }

        //Reset view
        if (jScrollPane != null) {
            mainPart.remove(jScrollPane);
        }
        if (mainLabel != null) {
            mainPart.remove(mainLabel);
        }
        if (filterPanel != null) {
            mainPart.remove(filterPanel.getPanel());
        }

        loadingPanel = new LoadingPanel(this.dim, "Building the tree rooted by " + path);
        mainPart.add(loadingPanel.getPanel(), BorderLayout.CENTER);
        mainPart.revalidate();

        getTreeWorker = new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {

                analyzer.buildFileTree(path, filter, thread, hash, recordInCache, maxDepth);
                analyzer.setInfoNode(analyzer.getRoot());
                return null;
            }

            @Override
            public void done() {
                if (analyzer.getRoot().getUserObject() instanceof String) {
                    displayNoFiles();
                } else {
                    //analyzer.setInfoNode(analyzer.getRoot());
                    displayTree();
                }
            }
        };

        // Call the SwingWorker from within the Swing thread
        getTreeWorker.execute();
    }

    public void displayNoFiles(){
        mainPart.remove(this.loadingPanel.getPanel());

        mainLabel = new JLabel("<html>Empty directory or no files corresponding to the filter.<br>" +
                                "Choose another directory please or change your filter.</html>");
        mainLabel.setVerticalAlignment(JLabel.CENTER);
        mainLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPart.add(mainLabel, BorderLayout.CENTER);

        mainPart.revalidate();
        mainPart.repaint();
    }

    public void displayTree() {
//        this.panel.remove(this.loadingPanel.getPanel());
//        this.panel.revalidate();
//        this.panel.repaint();
        mainPart.add(filterPanel.getPanel(), BorderLayout.NORTH);
        mainPart.remove(this.loadingPanel.getPanel());
        mainPart.revalidate();
        mainPart.repaint();

        TreeModel treeModel = analyzer.getTreeModel();

        OutlineModel mdl = DefaultOutlineModel.createOutlineModel(treeModel,
                new FileRowModel(), false, "File System");

        outline = new Outline();
        outline.setRenderDataProvider(new FileDataProvider());
        outline.setRootVisible(true);
        outline.setModel(mdl);

        ProgressBarRenderer pbr = new ProgressBarRenderer(0, 100);
        pbr.setStringPainted(true);
        //pbr.setForeground(new Color(179, 255, 165));
        outline.setDefaultRenderer(Integer.class,pbr);

        //outline.setQuickFilter(1, new Long(8466));
        //outline.setQuickFilter(0, new String("help-doc.html"));

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

    // a table cell renderer that displays a JProgressBar
    public class ProgressBarRenderer extends JProgressBar implements TableCellRenderer {
        public ProgressBarRenderer() {
            super();
        }

        public ProgressBarRenderer(BoundedRangeModel newModel) {
            super(newModel);
        }

        public ProgressBarRenderer(int orient) {
            super(orient);
        }

        public ProgressBarRenderer(int min, int max) {
            super(min, max);
        }

        public ProgressBarRenderer(int orient, int min, int max) {
            super(orient, min, max);
        }

        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {

            setValue((Integer) value);

            return this;
        }
    }

}
