package Analyzer.View.panels;

import Analyzer.Model.FileNode;
import Analyzer.Service.Analyzer;
import Analyzer.Service.Filter;
import Analyzer.Model.FileRowModel;
import Analyzer.View.aTrier.ContextMenu;
import Analyzer.View.aTrier.FileDataProvider;
import Analyzer.View.aTrier.ProgressBarRenderer;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TreePanel extends ZContainer implements Observer {

    public static final Integer ANNOUNCE_DUPLICATES = 0;
    public static final String firstStr = "Choose a directory, select your options and click start!";

    private boolean hash = false;
    private boolean recordInCache = false;
    private int maxDepth = Integer.MAX_VALUE;
    private boolean thread = false;

    private Analyzer analyzer;

    private Filter filter;
    private Filter filterAfter;
    private int depthSearch;

    private JPanel mainPart;
    private JLabel mainLabel;
    private LoadingPanel loadingPanel;
    private Dimension dim;
    private JScrollPane jScrollPane;
    private Outline outline;
    private FilterPanel filterPanel;
    private ContextMenu contextMenu;
    private SwingWorker<Void, Void> getTreeWorker;

    private JTabbedPane jTabbedPane;

    private String pathToBeSelected;

    private DefaultMutableTreeNode nodeSelected;

    public TreePanel(Dimension dim, Analyzer analyzer, JTabbedPane jTabbedPane, DuplicatesPanel duplicatesPanel) {
        super(dim);
        this.addObserver((Observer) duplicatesPanel);
        this.dim = dim;
        this.analyzer = analyzer;
        this.jTabbedPane = jTabbedPane;
        this.filter = new Filter();
        this.contextMenu = new ContextMenu(this, jTabbedPane);
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

    public int getDepthSearch() {
        return depthSearch;
    }

    public void setDepthSearch(int depthSearch) {
        this.depthSearch = depthSearch;
    }

    public String getPathToBeSelected() {
        return pathToBeSelected;
    }

    public void setPathToBeSelected(String pathToBeSelected) {
        this.pathToBeSelected = pathToBeSelected;
    }

    public DefaultMutableTreeNode getNodeSelected() {
        return nodeSelected;
    }

    public void setNodeSelected(DefaultMutableTreeNode nodeSelected) {
        this.nodeSelected = nodeSelected;
    }

    public Outline getOutline() {
        return outline;
    }

    @Override
    public void update(Observable o, Object arg) {

        if (o instanceof ActionsPanel) {
            ActionsPanel actionsPanel = (ActionsPanel) o;
            Integer iMessage = (Integer) arg;
            if (iMessage == ActionsPanel.START_SEARCH) {
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

                if (!actionsPanel.getOptionsPanel().getDepth().equals("")) {
                    this.setMaxDepth(Integer.parseInt(actionsPanel.getOptionsPanel().getDepth()));
                }

                this.setHash(actionsPanel.getOptionsPanel().getBoolean("hash"));
                this.setRecordInCache(actionsPanel.getOptionsPanel().getBoolean("cache"));
                this.setThread(actionsPanel.getOptionsPanel().getBoolean("thread"));
            }
        } else if (o instanceof FilterPanel) {
            FilterPanel filterPanel = (FilterPanel) o;
            Integer iMessage = (Integer) arg;

            if (iMessage.equals(FilterPanel.CHANGE_DEPTH_SEARCH)) {
                setDepthSearch(filterPanel.getDepth());
            } else if (iMessage.equals(FilterPanel.CHANGE_PATTERN_FILTER)) {
                String pattern = filterPanel.getPattern();
                filterAfter.setPattern(pattern);
                expandNodesUntilDepth(getDepthSearch());
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
                expandNodesUntilDepth(getDepthSearch());
                outline.setQuickFilter(FileRowModel.FILE_SYSTEM_COLUMN, filterAfter);

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
                expandNodesUntilDepth(getDepthSearch());
                outline.setQuickFilter(FileRowModel.FILE_SYSTEM_COLUMN, filterAfter);
            }
        }

    }

    public void initPanel() {
        filterPanel = new FilterPanel(this.dim, this);
        ActionsPanel actionsPanel = new ActionsPanel(this.dim, this);
        this.panel.add(actionsPanel.getPanel(), BorderLayout.NORTH);

        mainPart = new JPanel(new BorderLayout());

        mainLabel = new JLabel(firstStr);
        mainLabel.setFont(arialPlain);
        mainLabel.setVerticalAlignment(JLabel.CENTER);
        mainLabel.setHorizontalAlignment(JLabel.CENTER);
        mainPart.add(mainLabel, BorderLayout.CENTER);


        this.panel.add(mainPart, BorderLayout.CENTER);
    }

    public void getTree(String path) {


        System.out.println(getNbFiles(path));

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

//        SwingWorker<Long, Void> worker = new SwingWorker<Long, Void>() {
//            @Override
//            public Long doInBackground() {
//                long count = 0;
//                Path pathReal = Paths.get(path);
//                try {
//                    count = toStream(Files.newDirectoryStream(pathReal))
//                            .parallel()
//                            .flatMap(new FN<>()::apply)
//                            .count();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                System.out.println("count: " + count);
//                return count;
//            }
//
//            @Override
//            public void done() {
//                try {
//                    resultCount[0] = get();
//
//                } catch (InterruptedException | ExecutionException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        };
//
//        worker.execute();

        getTreeWorker = new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {
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

        //expandNodesUntilDepth(6);

        ProgressBarRenderer pbr = new ProgressBarRenderer(0, 100);
        //pbr.setForeground(new Color(179, 255, 165));
        outline.setDefaultRenderer(Float.class, pbr);

        jScrollPane = new JScrollPane(outline);
        mainPart.add(jScrollPane, BorderLayout.CENTER);

        outline.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int selectedRow = outline.rowAtPoint(e.getPoint());

                TreePath treePath = outline.getLayoutCache().getPathForRow(selectedRow);
                DefaultMutableTreeNode node = ((DefaultMutableTreeNode) treePath.getLastPathComponent());
                FileNode fileNode = (FileNode) node.getUserObject();
                setPathToBeSelected(fileNode.getAbsolutePath());
                setNodeSelected(node);
                if (selectedRow >= 0 && selectedRow < outline.getRowCount()) {
                    if (!outline.getSelectionModel().isSelectedIndex(selectedRow)) {
                        outline.setRowSelectionInterval(selectedRow, selectedRow);
                    }
                }
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable && fileNode.isDirectory()) {
                    showPopUp(e);
                }
            }

            private void showPopUp(MouseEvent e) {
                contextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

    public void announceDuplicates(){
        //Anouncing data change
        setChanged();
        notifyObservers(ANNOUNCE_DUPLICATES);
    }

    public void expandNodesUntilDepth(int depth) {
        for (int i = 0; i < outline.getLayoutCache().getRowCount(); i++) {
            System.out.println(outline.getLayoutCache().getPathForRow(i).toString());
            TreePath treePath = outline.getLayoutCache().getPathForRow(i);
            DefaultMutableTreeNode node = ((DefaultMutableTreeNode) treePath.getLastPathComponent());
            FileNode fileNode = (FileNode) node.getUserObject();

            if (treePath.getPathCount() == depth) {
                return;
            }

            if (fileNode.isDirectory()) {
                outline.expandPath(treePath);
            }
        }
    }

    public long getNbFiles(String path){
                        long count = 0;
                Path pathReal = Paths.get(path);
                try {
                    count = toStream(Files.newDirectoryStream(pathReal))
                            .parallel()
                            .flatMap(new FN<>()::apply)
                            .count();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("count: " + count);
                return count;
    }

    static class FN<T extends Path> implements Function<T, Stream<T>> {
        @Override
        public Stream<T> apply(T p) {
            if (!Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS)) {
                return Stream.of(p);
            } else {
                try {
                    return toStream(Files.newDirectoryStream(p)).flatMap(q -> apply((T) q));
                } catch (IOException ex) {
                    return Stream.empty();
                }
            }
        }
    }

    static <T> Stream<T> toStream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }

}
