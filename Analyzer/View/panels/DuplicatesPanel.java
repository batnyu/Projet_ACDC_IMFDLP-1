package Analyzer.View.panels;

import Analyzer.Model.FileTree;
import Analyzer.Service.Analyzer;
import Analyzer.Service.Filter;
import sun.reflect.generics.tree.Tree;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DuplicatesPanel extends ZContainer implements Observer {

    private ActionsPanel actionsPanel;

    class FileIndex {

        File file;
        int index;

        public FileIndex(File file, int index) {
            this.file = file;
            this.index = index;
        }

        public File getFile() {
            return file;
        }

        public int getIndex() {
            return index;
        }
    }

    private Dimension dim;

    private Analyzer analyzer;
    private Filter filter;
    private Map<String, List<File>> duplicates;

    final JFileChooser fc = new JFileChooser();
    private JLabel mainLabel;
    private JTable jTable;
    private JScrollPane jScrollPane;
    private LoadingPanel loadingPanel;

    private ArrayList<FileIndex> filesToDelete;

    private String previousHash;
    private Color currentColor;
    private final Random random = new Random(2);

    public DuplicatesPanel(Dimension dim, Analyzer analyzer) {
        super(dim);
        this.dim = dim;
        this.analyzer = analyzer;
        this.filter = new Filter();
        this.filesToDelete = new ArrayList<FileIndex>();
        this.currentColor = getNext();
        initPanel();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof ActionsPanel) {
            ActionsPanel actionsPanel = (ActionsPanel) o;
            Integer iMessage = (Integer) arg;

            if (iMessage == ActionsPanel.START_SEARCH) {
                getDuplicates(actionsPanel.getCurrentSelectedFilePath());
            } else if (iMessage == ActionsPanel.CHANGE_OPTIONS) {
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
                    filter = new Filter();
                }
            } else if (iMessage == ActionsPanel.DELETING_SELECTED_FILES) {
                if (jTable != null) {
                    for (int i = 0; i < jTable.getRowCount(); i++) {
                        Boolean isChecked = Boolean.valueOf(jTable.getValueAt(i, 0).toString());

                        if (isChecked) {
                            System.out.println("checked " + i);
                            filesToDelete.add(new FileIndex(new File(jTable.getValueAt(i, 2).toString()), i));

                        } else {
                            System.out.printf("Row %s is not checked \n", i);
                        }
                    }

                    Object[] options = {"Yes", "No"};
                    int dialogResult = JOptionPane.showOptionDialog(null,
                            "Are you sure you want to delete these files?",
                            "Warning",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,     //do not use a custom Icon
                            options,  //the titles of buttons
                            options[0]); //default button title
                    if (dialogResult == JOptionPane.YES_OPTION) {
                        for (FileIndex fileIndex : filesToDelete) {
                            System.out.println(fileIndex.getFile().getAbsolutePath());
                            try {
                                FileTree.delete(fileIndex.getFile());
                                ((DefaultTableModel) jTable.getModel()).removeRow(fileIndex.getIndex());
                                panel.revalidate();

                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }

                }
            }
        } else if(o instanceof TreePanel){
            TreePanel treePanel = ((TreePanel) o);
            Integer iMessage = (Integer) arg;
            if (iMessage == TreePanel.ANNOUNCE_DUPLICATES) {
                actionsPanel.setSelectedPath(treePanel.getPathToBeSelected());
                actionsPanel.setStartButtonEnabled();
                getDuplicatesFromTree(treePanel.getNodeSelected());
            }
        }
    }

    public void initPanel() {

        actionsPanel = new ActionsPanel(this.dim, this);
        this.panel.add(actionsPanel.getPanel(), BorderLayout.NORTH);
        loadingPanel = new LoadingPanel(this.dim, "");
        mainLabel = new JLabel(TreePanel.firstStr);
        mainLabel.setFont(arialPlain);
        mainLabel.setVerticalAlignment(JLabel.CENTER);
        mainLabel.setHorizontalAlignment(JLabel.CENTER);
        this.panel.add(mainLabel, BorderLayout.CENTER);

    }

    public void getDuplicatesFromTree(DefaultMutableTreeNode node){
        duplicates = analyzer.getDuplicates(node, new Filter());

        resetView();

        displayDuplicates(actionsPanel.getCurrentSelectedFilePath());
    }

    public void getDuplicates(String path) {
        resetView();


        loadingPanel.setTextLabel("Searching for duplicates in " + path);
        this.panel.add(loadingPanel.getPanel(), BorderLayout.CENTER);
        this.panel.revalidate();

        SwingWorker<Map<String, List<File>>, Void> worker = new SwingWorker<Map<String, List<File>>, Void>() {
            @Override
            public Map<String, List<File>> doInBackground() {

                Map<String, List<File>> dup = analyzer.getDuplicates(path, filter);
                return dup;
            }

            @Override
            public void done() {
                try {
                    duplicates = get();

                    displayDuplicates(path);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                } catch (ExecutionException ex) {
                    ex.printStackTrace();
                }
            }
        };

        // Call the SwingWorker from within the Swing thread
        worker.execute();
    }

    private void resetView() {
        //Reset view
        if (jScrollPane != null) {
            this.panel.remove(jScrollPane);
        }
        if (mainLabel != null) {
            this.panel.remove(mainLabel);
        }
    }

    public void displayDuplicates(String path) {

        this.panel.remove(this.loadingPanel.getPanel());
        this.panel.revalidate();
        this.panel.repaint();

        Object[] columnNames = {"Selection", "Hash", "Path"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column != 0) {
                    return false;
                }
                return true;
            }

            public Class<?> getColumnClass(int colIndex) {
                return getValueAt(0, colIndex).getClass();
            }
        };


        if (duplicates.isEmpty()) {
            System.out.println("No duplicates are contained in \"" + path + "\".");
            mainLabel.setText("No duplicates are contained in \"" + path + "\".");
            this.panel.add(mainLabel, BorderLayout.CENTER);
        } else {
            System.out.println("Duplicates contained in \"" + path + "\" are: ");
            this.panel.remove(mainLabel);
            for (String vHash : duplicates.keySet()) {
                for (File file : duplicates.get(vHash)) {
                    //System.out.println(vHash + " -> " + file.getAbsolutePath());
                    Vector row = new Vector();
                    //row.add(new Boolean(false));
                    row.add(false);
                    row.add(vHash);
                    row.add(file.getAbsolutePath());

                    model.addRow(row);
                }
            }

            jTable = new JTable(model) {
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row,
                                                 int column) {
                    Component component = super.prepareRenderer(renderer, row, column);
                    // Set auto width
                    int rendererWidth = component.getPreferredSize().width;
                    TableColumn tableColumn = getColumnModel().getColumn(column);
                    tableColumn.setPreferredWidth(Math.max(rendererWidth +
                                    getIntercellSpacing().width,
                            tableColumn.getPreferredWidth()));

                    // Set backgrounds color
                    String currentHash = (String) getValueAt(row, 1);
                    if (row == 0 && column == 0) {
                        previousHash = (String) getValueAt(row, 1);
                    } else if (!previousHash.equals(currentHash)) {
                        if (currentColor == Color.white) {
                            currentColor = Color.lightGray;
                        } else {
                            currentColor = Color.white;
                        }
                        previousHash = currentHash;

                    }

                    component.setBackground(currentColor);

                    return component;
                }

            };

            jScrollPane = new JScrollPane(jTable);
            this.panel.add(jScrollPane, BorderLayout.CENTER);
        }

        this.panel.revalidate();
    }

    public Color getNext() {

        int[] colorInt = new int[3];
        colorInt[0] = random.nextInt(128) + 127;
        colorInt[1] = random.nextInt(128) + 127;
        colorInt[2] = random.nextInt(128) + 127;

        return new Color(colorInt[0], colorInt[1], colorInt[2], 255);
    }

}
