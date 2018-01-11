package Analyzer.View.panels;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Observer;

public class ActionsPanel extends ZContainer {

    public static final Integer START_SEARCH = 0;
    public static final Integer CHANGE_OPTIONS = 1;
    public static final Integer DELETING_SELECTED_FILES = 2;
    public static final Integer STOP_SEARCH = 3;

    private final JFileChooser fc;

    private String currentSelectedFilePath;
    private OptionsPanel optionsPanel;
    private ZContainer container;
    private JPanel go;
    private JLabel selectedPath;
    private JButton start;
    private JButton stop;

    public ActionsPanel(Dimension dim, ZContainer container) {
        super(dim);
        this.addObserver((Observer) container);
        this.container = container;
        fc = new JFileChooser();
        selectedPath = new JLabel("<html><strong>Selected path:</strong> <i>none</i></html>");
        initPanel();
    }

    public String getCurrentSelectedFilePath() {
        return currentSelectedFilePath;
    }

    public OptionsPanel getOptionsPanel() {
        return optionsPanel;
    }

    public void setSelectedPath(String path) {
        this.currentSelectedFilePath = path;
        this.selectedPath.setText("<html><strong>Selected path:</strong> <i>" + currentSelectedFilePath + "</i></html>");
    }

    public void setStartButtonEnabled(){
        this.start.setEnabled(true);
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
                    selectedPath.setText("<html><strong>Selected path:</strong> <i>" + currentSelectedFilePath + "</i></html>");
                    start.setEnabled(true);
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
                    notifyObservers(CHANGE_OPTIONS);
                }
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(selectDirectory);
        buttons.add(editOptions);

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

            buttons.add(deleteSelected);
        }

        this.panel.add(buttons,BorderLayout.NORTH);

        go = new JPanel(new FlowLayout(FlowLayout.LEFT));
        go.add(selectedPath);
        start = new JButton("Start");
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Anouncing data change
                setChanged();
                notifyObservers(START_SEARCH);
                stop.setEnabled(true);
            }
        });
        start.setEnabled(false);
        go.add(start);

        stop = new JButton("Stop");
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Anouncing data change
                setChanged();
                notifyObservers(STOP_SEARCH);
                stop.setEnabled(false);
            }
        });
        stop.setEnabled(false);
        go.add(stop);

        this.panel.add(go, BorderLayout.CENTER);
    }
}
