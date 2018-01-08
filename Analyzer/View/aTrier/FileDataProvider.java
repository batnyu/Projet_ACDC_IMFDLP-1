package Analyzer.View.aTrier;

import Analyzer.Model.FileNode;
import org.netbeans.swing.outline.RenderDataProvider;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class FileDataProvider implements RenderDataProvider {
    public java.awt.Color getBackground(Object o) {
        return null;
    }

    public String getDisplayName(Object o) {
        DefaultMutableTreeNode node = ((DefaultMutableTreeNode) o);
        FileNode fileNode = ((FileNode) node.getUserObject());

        if (fileNode.isDirectory()) {
            return "<strong>" + fileNode.getName() + "</strong>";
        }
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
        DefaultMutableTreeNode node = ((DefaultMutableTreeNode) o);
        FileNode fileNode = ((FileNode) node.getUserObject());

        if (fileNode.isDirectory()) {
            return true;
        }
        return false;
    }
}
