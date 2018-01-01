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
import java.util.Observer;

public class ActionsPanel extends ZContainer {

    public static final Integer CHANGE_SELECTED_FILE = 0;
    public static final Integer CHANGE_FILTER = 1;
    public static final Integer DELETING_SELECTED_FILES = 2;

    final JFileChooser fc;

    String currentSelectedFilePath;
    OptionsPanel optionsPanel;
    ZContainer container;

    public ActionsPanel(Dimension dim, ZContainer container) {
        super(dim);
        this.addObserver((Observer) container);
        this.container = container;
        fc = new JFileChooser();
        initPanel();
    }

    public String getCurrentSelectedFilePath() {
        return currentSelectedFilePath;
    }

    public OptionsPanel getOptionsPanel() {
        return optionsPanel;
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
                    currentSelectedFilePath = file.getAbsolutePath();
                    //Anouncing data change
                    setChanged();
                    notifyObservers(CHANGE_SELECTED_FILE);

                } else {
                    System.out.println("Open command cancelled by user.");
                }
            }
        });

        optionsPanel = new OptionsPanel(null, this.container);

        JButton editOptions = new JButton("Edit options");
        editOptions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int result = JOptionPane.showConfirmDialog(null, optionsPanel.getPanel(),
                        "Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (result == JOptionPane.OK_OPTION) {
/*                    System.out.println("pattern: " + optionsPanel.getPattern());
                    System.out.println("Weight: " + optionsPanel.getWeightInfo().getTextField().getText());
                    System.out.println("Date: " + optionsPanel.getDateInfo().getTextField().getText());
                    System.out.println("Date truc: " + optionsPanel.getSelectedButtonText(optionsPanel.getDateInfo().getButtonGroup()));*/
                    //Anouncing data change
                    setChanged();
                    notifyObservers(CHANGE_FILTER);
                }
            }
        });

        this.panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.panel.add(selectDirectory);
        this.panel.add(editOptions);

        if (this.container instanceof DuplicatesPanel) {
            JButton deleteSelected = new JButton("Delete selected");
            deleteSelected.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //Anouncing data change
                    setChanged();
                    notifyObservers(DELETING_SELECTED_FILES);
                }
            });

            this.panel.add(deleteSelected);
        }
    }
}
