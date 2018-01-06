package Analyzer.Model;

import Analyzer.Model.FileNode;
import org.netbeans.swing.outline.RowModel;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.VariableHeightLayoutCache;
import java.util.Date;

public class FileRowModel implements RowModel {
    @Override
    public Class getColumnClass(int column) {
        switch (column) {
            case 0:
                return Long.class;
            case 1:
                return Date.class;
            default:
                assert false;
        }
        return null;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int column) {
        return column == 0 ? "Date" : "Size";
    }

    @Override
    public Object getValueFor(Object node, int column) {
        DefaultMutableTreeNode mutableTreeNode = ((DefaultMutableTreeNode) node);
        FileNode fileNode = (FileNode) mutableTreeNode.getUserObject();
        switch (column) {
            case 0:
                return new Long(fileNode.getSize());
            case 1:
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
