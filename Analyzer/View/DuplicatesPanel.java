package Analyzer.View;

import Analyzer.Model.FileTree;
import Analyzer.Service.Analyzer;
import Analyzer.Service.Filter;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

public class DuplicatesPanel extends ZContainer {

    Dimension dim;

    Analyzer analyzer;
    Filter filter;
    private Map<String, List<File>> duplicates;

    final JFileChooser fc = new JFileChooser();
    JLabel mainLabel;
    JTable jTable;
    JScrollPane jScrollPane;
    LoadingPanel loadingPanel;

    ArrayList<FileIndex> filesToDelete;

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


    public DuplicatesPanel(Dimension dim, Analyzer analyzer) {
        super(dim);
        this.dim = dim;
        this.analyzer = analyzer;
        this.filter = new Filter();
        initPanel();
    }

    public void initPanel() {

        filesToDelete = new ArrayList<FileIndex>();

        fc.setCurrentDirectory(new File
                (System.getProperty("user.home") + System.getProperty("file.separator")));

        fc.setCurrentDirectory(FileSystemView.getFileSystemView().getRoots()[0]);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        JButton selectDirectory = new JButton("Select directory");
        selectDirectory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showOpenDialog(selectDirectory);
                System.out.println("returnVal = " + returnVal);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    System.out.println("Opening: " + file.getAbsolutePath());
/*                    container.removeAll();
                    container.add(new BottomPanel(size).getPanel(),BorderLayout.SOUTH);
                    container.add(new TreePanel(size).getPanel(), BorderLayout.CENTER);
                    container.revalidate();*/

                    getDuplicates(file.getAbsolutePath());
                } else {
                    System.out.println("Open command cancelled by user.");
                }
            }
        });

        OptionsPanel optionsPanel = new OptionsPanel(null);

        JButton editOptions = new JButton("Edit options");
        editOptions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int result = JOptionPane.showConfirmDialog(null, optionsPanel.getPanel(),
                        "Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
                    System.out.println("pattern: " + optionsPanel.getPattern());
                    System.out.println("Weight: " + optionsPanel.getWeightInfo().getTextField().getText());
                    System.out.println("Date: " + optionsPanel.getDateInfo().getTextField().getText());
                    System.out.println("Date truc: " + optionsPanel.getSelectedButtonText(optionsPanel.getDateInfo().getButtonGroup()));

                    if (!optionsPanel.getPattern().equals("")) {
                        filter.setPattern(optionsPanel.getPattern());
                    }

                    if (optionsPanel.getWeight() != 0) {
                        switch (optionsPanel.getSymbol("weight")) {
                            case "=":
                                filter.weightEq(optionsPanel.getWeight());
                                break;
                            case "<":
                                filter.weightLw(optionsPanel.getWeight());
                                break;
                            case ">":
                                filter.weightGt(optionsPanel.getWeight());
                            default:
                                break;
                        }
                    }

                    if (optionsPanel.getDate() != null) {
                        switch (optionsPanel.getSymbol("date")) {
                            case "=":
                                filter.dateEq(optionsPanel.getDate());
                                break;
                            case "<":
                                filter.dateLw(optionsPanel.getDate());
                                break;
                            case ">":
                                filter.dateGt(optionsPanel.getDate());
                            default:
                                break;
                        }
                    }

                    //Reset filter when no fields completed
                    if(optionsPanel.getPattern().equals("") && optionsPanel.getWeight() == 0 && optionsPanel.getDate() == null){
                        filter = new Filter();
                    }
                }
            }
        });

        JButton deleteSelected = new JButton("Delete selected");
        deleteSelected.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
                                revalidate();

                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }

                }
            }
        });

        JPanel jPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jPanel.add(selectDirectory);
        jPanel.add(editOptions);
        jPanel.add(deleteSelected);
        this.panel.add(jPanel, BorderLayout.NORTH);

        mainLabel = new JLabel("Choose a directory");
        mainLabel.setVerticalAlignment(JLabel.CENTER);
        mainLabel.setHorizontalAlignment(JLabel.CENTER);
        this.panel.add(mainLabel, BorderLayout.CENTER);

    }

    public void getDuplicates(String path) {

        //Reset view
        if (jScrollPane != null) {
            this.panel.remove(jScrollPane);
        }
        if(mainLabel != null) {
            this.panel.remove(mainLabel);
        }

        loadingPanel = new LoadingPanel(this.dim,"Searching for duplicates in " + path);
        this.panel.add(loadingPanel.getPanel(),BorderLayout.CENTER);
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
/*
                currentHash = (String)getValueAt(row, 1);
                System.out.println("currentHash = " + currentHash);
                component.setBackground(Color.white);

                if (previousHash.equals(currentHash)) {
                    component.setBackground(Color.lightGray);
                } else {
                    previousHash = currentHash;
                }*/

                    return component;
                }

            };

            jScrollPane = new JScrollPane(jTable);
            this.panel.add(jScrollPane, BorderLayout.CENTER);
        }

        this.panel.revalidate();
    }

}
