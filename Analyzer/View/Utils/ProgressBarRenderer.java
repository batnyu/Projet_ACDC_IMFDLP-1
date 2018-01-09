package Analyzer.View.Utils;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;

// a table cell renderer that displays a JProgressBar
public class ProgressBarRenderer extends JProgressBar implements TableCellRenderer {
    public ProgressBarRenderer() {
        super();
    }

    public ProgressBarRenderer(BoundedRangeModel newModel) {
        super(newModel);
    }

    public ProgressBarRenderer(int orient) {
        super(orient);
    }

    public ProgressBarRenderer(int min, int max) {
        super(min, max);
        setStringPainted(true);
    }

    public ProgressBarRenderer(int orient, int min, int max) {
        super(orient, min, max);
    }

    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(1);
        String valueStr = df.format(value);
        setString(valueStr + "%");

        setValue(Math.round((Float) value));

        return this;
    }

    @Override
    public String toString() {
        return "<html><strong>" + super.toString() + "</strong></html>";
    }
}
