package Analyzer.View;

import Analyzer.Model.FileTree;
import Analyzer.Service.Filter;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ActionsPanel extends ZContainer {

    String context;

    Filter filter;
    final JFileChooser fc;
    JLabel label = new JLabel("Choose a directory");
    JTable jTable;
    JScrollPane jScrollPane;

    ArrayList<FileIndex> filesToDelete = new ArrayList<FileIndex>();

    public ActionsPanel(Dimension dim, String context) {
        super(dim);
        this.context = context;
        filter = new Filter();
        fc = new JFileChooser();
        initPanel();
    }

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

    public void initPanel() {

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

//                    displayDuplicates(file.getAbsolutePath());
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

                    filter.setPattern(optionsPanel.getPattern());

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

        label.setVerticalAlignment(JLabel.NORTH);
        this.panel.add(label, BorderLayout.CENTER);
    }
}
