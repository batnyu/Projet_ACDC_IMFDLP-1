package Analyzer.View;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;

public abstract class ZContainer extends Observable {
    protected JPanel panel;

    protected Font comics30 = new Font("Comics Sans MS", Font.BOLD, 30);
    protected Font comics40 = new Font("Comics Sans MS", Font.BOLD, 40);
    protected Font arialBold = new Font("Arial", Font.BOLD, 15);
    protected Font arialPlain = new Font("Arial", Font.PLAIN, 15);
    protected Font dialog = new Font("Dialog", Font.BOLD + Font.ITALIC, 15);

    public ZContainer(Dimension dim) {
        this.panel = new JPanel();
        //this.panel.setPreferredSize(dim);
        this.panel.setLayout(new BorderLayout());
        this.panel.setBackground(Color.white);
        //this.panel.setBorder(BorderFactory.createLineBorder(Color.red));

    }

    protected JPanel getPanel() {
        return this.panel;
    }

    protected abstract void initPanel();

}
