package Analyzer.View.panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

public class ErrorsPanel extends ZContainer {

    private ArrayList<String> errors;
    private Dimension dim;
    private JLabel mainLabel;
    private JTable jTable;
    private JScrollPane jScrollPane;

    public ErrorsPanel(Dimension dim, ArrayList<String> errors) {
        super(dim);
        this.dim = dim;
        this.errors = errors;

        initPanel();
    }

    public ArrayList<String> getErrors() {
        return errors;
    }

    public void initPanel() {

        this.panel.setPreferredSize(new Dimension(800,350));

        mainLabel = new JLabel("No errors!");
        mainLabel.setFont(arialBold);
        mainLabel.setVerticalAlignment(JLabel.CENTER);
        mainLabel.setHorizontalAlignment(JLabel.CENTER);
        this.panel.add(mainLabel, BorderLayout.CENTER);

        if(!getErrors().isEmpty()){
            this.panel.remove(mainLabel);

            Object[] columnNames = {"Exception", "Path"};
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

            for (String error : getErrors()) {
                String[] split = error.split(": ",2);

                Vector row = new Vector();
                row.add(split[0]);
                row.add(split[1]);

                model.addRow(row);
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
                    return component;
                }

            };

            jScrollPane = new JScrollPane(jTable);
            this.panel.add(jScrollPane, BorderLayout.CENTER);
        }
    }
}
