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
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

public class TreePanel extends ZContainer implements Observer {

    private boolean hash = false;
    private boolean recordInCache = false;
    private int maxDepth = Integer.MAX_VALUE;
    private boolean thread = false;

    Analyzer analyzer;
    private Filter filter;
    private Filter filterAfter;

    private JPanel mainPart;
    private JLabel mainLabel;
    private LoadingPanel loadingPanel;
    private Dimension dim;
    private JScrollPane jScrollPane;
    private Outline outline;
    private FilterPanel filterPanel;
    private ContextMenu contextMenu;
    private SwingWorker<Void, Void> getTreeWorker;

    public TreePanel(Dimension dim, Analyzer analyzer) {
        super(dim);
        this.dim = dim;
        this.analyzer = analyzer;
        this.filter = new Filter();
        this.contextMenu = new ContextMenu(this);
        initPanel();
    }

    public boolean isHash() {
        return hash;
    }

    public void setHash(boolean hash) {
        this.hash = hash;
    }

    public boolean isRecordInCache() {
        return recordInCache;
    }

    public void setRecordInCache(boolean recordInCache) {
        this.recordInCache = recordInCache;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public boolean isThread() {
        return thread;
    }

    public void setThread(boolean thread) {
        this.thread = thread;
    }

    public Outline getOutline() {
        return outline;
    }

    @Override
    public void update(Observable o, Object arg) {

        if (o instanceof ActionsPanel) {
            ActionsPanel actionsPanel = (ActionsPanel) o;
            Integer iMessage = (Integer) arg;
            if (iMessage == ActionsPanel.CHANGE_SELECTED_FILE) {
                this.filterAfter = new Filter();
                getTree(actionsPanel.getCurrentSelectedFilePath());
            } else if (iMessage == ActionsPanel.CHANGE_OPTIONS) {
                System.out.println("pattern: " + actionsPanel.getOptionsPanel().getPattern());
                System.out.println("Weight: " + actionsPanel.getOptionsPanel().getWeightInfo().getTextField().getText());
                System.out.println("Weight condition: " + actionsPanel.getOptionsPanel().getSelectedButtonText(actionsPanel.getOptionsPanel().getWeightInfo().getButtonGroup()));
                System.out.println("Date: " + actionsPanel.getOptionsPanel().getDateInfo().getTextField().getText());
                System.out.println("Date condition: " + actionsPanel.getOptionsPanel().getSelectedButtonText(actionsPanel.getOptionsPanel().getDateInfo().getButtonGroup()));
//                System.out.println("Depth: " + actionsPanel.getOptionsPanel().getDepth());
//                System.out.println("Record in cache: " + actionsPanel.getOptionsPanel().getBoolean("cache"));
//                System.out.println("Hash: " + actionsPanel.getOptionsPanel().getBoolean("hash"));
//                System.out.println("Multi-thread: " + actionsPanel.getOptionsPanel().getBoolean("thread"));

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
                            System.out.println("allo ? " + actionsPanel.getOptionsPanel().getWeight());
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

                if(!actionsPanel.getOptionsPanel().getDepth().equals("")){
                    this.setMaxDepth(Integer.parseInt(actionsPanel.getOptionsPanel().getDepth()));
                }

                this.setHash(actionsPanel.getOptionsPanel().getBoolean("hash"));
                this.setRecordInCache(actionsPanel.getOptionsPanel().getBoolean("cache"));
                this.setThread(actionsPanel.getOptionsPanel().getBoolean("thread"));
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
                    if (!filterPanel.getDate().equals("noDate")) {
                        date = sourceFormat.parse(filterPanel.getDate());
                    } else {
                        date = new Date(0);
                    }
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
                System.out.println("isThread() = " + isThread());
                System.out.println("isRecordInCache() = " + isRecordInCache());
                System.out.println("isHash() = " + isHash());
                System.out.println("getMaxDepth() = " + getMaxDepth());
                analyzer.buildFileTree(path, filter, isThread(), isHash(), isRecordInCache(), getMaxDepth());
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

    public void displayNoFiles() {
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
        outline.setDefaultRenderer(Float.class, pbr);

        //outline.setQuickFilter(1, new Long(8466));
        //outline.setQuickFilter(0, new String("help-doc.html"));

        jScrollPane = new JScrollPane(outline);
        mainPart.add(jScrollPane, BorderLayout.CENTER);

        outline.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int selectedRow = outline.rowAtPoint(e.getPoint());
                if (selectedRow >= 0 && selectedRow < outline.getRowCount()) {
                    if (!outline.getSelectionModel().isSelectedIndex(selectedRow)) {
                        outline.setRowSelectionInterval(selectedRow, selectedRow);
                    }
                }
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
                    showPopUp(e);
                }
            }

            private void showPopUp(MouseEvent e) {
                contextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        Filter filter1 = new Filter();
        filter1.setPattern("html");
        filter1.weightLw(6000);
        outline.setQuickFilter(FileRowModel.FILE_SYSTEM_COLUMN, filter1);
        outline.setQuickFilter(FileRowModel.WEIGHT_COLUMN, filter1);

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

            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(1);
            String valueStr = df.format(value);
            setString(valueStr + "%");
            setValue(Math.round((Float) value));

            return this;
        }
    }

    class ContextMenu extends JPopupMenu {
        private JMenuItem item;
        private TreePanel tableClass;  // dirty direct reference *****

        public ContextMenu(TreePanel tableClass){
            this.tableClass = tableClass;
            this.item= new JMenuItem("Search for duplicates");

            this.item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int row = tableClass.getOutline().getSelectedRow();
                    Outline table = tableClass.getOutline();
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)table.getValueAt(row, FileRowModel.FILE_SYSTEM_COLUMN);
                    FileNode fileNode = (FileNode) node.getUserObject();
                    System.out.println(fileNode.getAbsolutePath());
                }
            });

            add(item);
        }
    }

}
