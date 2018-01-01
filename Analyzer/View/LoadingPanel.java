package Analyzer.View;

import javax.swing.*;
import java.awt.*;

public class LoadingPanel extends ZContainer{

    String msg;
    JLabel loadingMsg;
    JProgressBar progressBar;

    public LoadingPanel(Dimension dim, String msg) {
        super(dim);
        this.msg = msg;
        initPanel();
    }

    public void initPanel() {
        this.panel.setLayout(new BoxLayout(this.panel,BoxLayout.Y_AXIS));

        this.loadingMsg = new JLabel(this.msg);
        this.loadingMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.panel.add(this.loadingMsg);

        this.progressBar = new JProgressBar(0, 100);
        this.progressBar.setIndeterminate(true);
        this.progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.panel.add(progressBar);

    }
}
