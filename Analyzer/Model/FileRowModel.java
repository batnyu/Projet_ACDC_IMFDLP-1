package Analyzer.Model;

import org.netbeans.swing.outline.RowModel;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Date;

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
                return "Last modified";
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
                if(fileNode.isDirectory()){
                    return "<html><strong>" + fileNode.getSize() + "</strong></html>";
                } else {
                    return fileNode.getSize();
                }
            case 1:
                if(fileNode.isDirectory()){
                    return "<html><strong>" + fileNode.getNumberFiles() + "</strong></html>";
                } else {
                    return fileNode.getNumberFiles();
                }
            case 2:
                if(fileNode.isDirectory()){
                    return "<html><strong>" + fileNode.getNumberFolders() + "</strong></html>";
                } else {
                    return fileNode.getNumberFolders();
                }
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
