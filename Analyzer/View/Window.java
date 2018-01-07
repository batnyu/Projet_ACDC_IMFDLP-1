package Analyzer.View;

import Analyzer.Model.FileTree;
import Analyzer.Service.Analyzer;
import com.jgoodies.looks.windows.WindowsLookAndFeel;
import org.jdesktop.xswingx.demo.Demo;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

public class Window extends JFrame {

    private JMenuBar menu = null;
    private JMenu file = null;
    private JMenuItem selectDirectory = null;

    final JFileChooser fc = new JFileChooser();

    private JPanel container = new JPanel();
    private Dimension size;

    Analyzer analyzer;

    TreePanel treePanel;
    DuplicatesPanel duplicatesPanel;
    BottomPanel bottomPanel;

    public Window() {
        this.setTitle("Il me faut de la place !");
        this.setSize(1200, 600);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        //this.setResizable(false);

        UIManager.installLookAndFeel("JGoodies Windows", WindowsLookAndFeel.class.getName());
        try {
            UIManager.setLookAndFeel("com.jgoodies.looks.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        //Change text color of ProgressBar
        UIManager.put("ProgressBar.selectionForeground", Color.white);
        UIManager.put("ProgressBar.selectionBackground", Color.black);

        this.size = new Dimension(this.getWidth(), this.getHeight());

        analyzer = new FileTree();

        fc.setCurrentDirectory(new File
                (System.getProperty("user.home") + System.getProperty("file.separator")));

        fc.setCurrentDirectory(FileSystemView.getFileSystemView().getRoots()[0]);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        menu = new JMenuBar();



        file = new JMenu("File");
        file.setMnemonic('f');

        selectDirectory = new JMenuItem("Select directory");
        selectDirectory.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));

/*        selectDirectory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showOpenDialog(container);
                System.out.println("returnVal = " + returnVal);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    System.out.println("Opening: " + file.getAbsolutePath());
                    container.removeAll();
                    container.add(new BottomPanel(size).getPanel(),BorderLayout.SOUTH);
                    container.add(new TreePanel(size).getPanel(), BorderLayout.CENTER);
                    container.revalidate();
                } else {
                    System.out.println("Open command cancelled by user.");
                }
            }
        });*/

        file.add(selectDirectory);
        //file.addSeparator();

        menu.add(file);
        menu.add(createLookAndFeelMenu(this));

        //this.container.setBackground(Color.pink);
        this.container.setLayout(new BorderLayout());


        JTabbedPane tabbedPane = new JTabbedPane();

        treePanel = new TreePanel(size, analyzer);
        duplicatesPanel = new DuplicatesPanel(size, analyzer);

        tabbedPane.addTab("Scan", treePanel.getPanel());
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        tabbedPane.addTab("Duplicates", duplicatesPanel.getPanel());
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        this.container.add(tabbedPane);

        bottomPanel = new BottomPanel(size);
        analyzer.addErrorHandler(bottomPanel);

        this.container.add(bottomPanel.getPanel(), BorderLayout.SOUTH);

        this.setContentPane(this.container);

        this.setJMenuBar(menu);
    }

    public static JMenu createLookAndFeelMenu(final Component toUpdate) {
        final JMenu lnf = new JMenu("Look and Feel");
        for (final UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            String name = info.getName();
            if (name.equals("Nimbus") || name.equals("JGoodies Windows")) {
                final JMenuItem mi = new JMenuItem(name);
                lnf.add(mi);

                mi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            UIManager.setLookAndFeel(info.getClassName());
                            System.out.println(info.getClassName());
                            SwingUtilities.updateComponentTreeUI(toUpdate);
                        } catch (Exception ex) {
                            mi.setEnabled(false);
                            ex.printStackTrace();
                        }
                    }
                });
            }
        }
        return lnf;
    }
}
