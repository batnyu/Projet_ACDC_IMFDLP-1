package Analyzer.View.Utils;

import Analyzer.Model.FileNode;
import Analyzer.Model.FileRowModel;
import Analyzer.View.panels.TreePanel;
import org.netbeans.swing.outline.Outline;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ContextMenu extends JPopupMenu {
    private JMenuItem item;
    private TreePanel treePanel;  // dirty direct reference *****
    private JTabbedPane jTabbedPane;

    public ContextMenu(TreePanel treePanel, JTabbedPane jTabbedPane) {
        this.treePanel = treePanel;
        this.jTabbedPane = jTabbedPane;
        this.item = new JMenuItem("Search for duplicates");

        this.item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = treePanel.getOutline().getSelectedRow();
                Outline table = treePanel.getOutline();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) table.getValueAt(row, FileRowModel.FILE_SYSTEM_COLUMN);
                FileNode fileNode = (FileNode) node.getUserObject();
                System.out.println(fileNode.getAbsolutePath());
                jTabbedPane.setSelectedIndex(1);
                treePanel.announceDuplicates();
            }
        });

        add(item);
    }
}