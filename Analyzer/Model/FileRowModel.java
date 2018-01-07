package Analyzer.Model;

import Analyzer.Model.FileNode;
import org.netbeans.swing.outline.RowModel;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.VariableHeightLayoutCache;
import java.util.Date;

import static java.lang.Math.toIntExact;

public class FileRowModel implements RowModel {

    public static final Integer FILE_SYSTEM_COLUMN = 0;
    public static final Integer WEIGHT_COLUMN = 1;
    public static final Integer DATE_COLUMN = 5;

    @Override
    public Class getColumnClass(int column) {
        switch (column) {
            case 0:
                return Long.class;
            case 1:
                return Long.class;
            case 2:
                return Long.class;
            case 3:
                return Float.class;
            case 4:
                return Date.class;
            default:
                assert false;
        }
        return null;
    }

    @Override
    public int getColumnCount() {
        return 5;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Size";
            case 1:
                return "Files";
            case 2:
                return "Folders";
            case 3:
                return "% of Parent (Size)";
            case 4:
                return "Date";
            default:
                assert false;
        }
        return null;
    }

    @Override
    public Object getValueFor(Object node, int column) {
        DefaultMutableTreeNode mutableTreeNode = ((DefaultMutableTreeNode) node);
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) mutableTreeNode.getParent();
        FileNode fileNode = (FileNode) mutableTreeNode.getUserObject();
        float sizeParent;
        if (!mutableTreeNode.isRoot()) {
            sizeParent = (float)((FileNode) parent.getUserObject()).getSize();
        } else {
            sizeParent = (float)fileNode.getSize();
        }

        switch (column) {
            case 0:
                return fileNode.getSize();
            case 1:
                return fileNode.getNumberFiles();
            case 2:
                return fileNode.getNumberFolders();
            case 3:
                return (fileNode.getSize() * 100) / sizeParent;
            case 4:
                return new Date(fileNode.lastModified());
            default:
                assert false;
        }
        return null;
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        return false;
    }

    @Override
    public void setValueFor(Object node, int column, Object value) {
        //do nothing for now
    }
}
