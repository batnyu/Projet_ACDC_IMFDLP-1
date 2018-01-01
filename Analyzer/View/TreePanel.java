package Analyzer.View;

import Analyzer.Service.Analyzer;

import javax.swing.*;
import java.awt.*;

public class TreePanel extends ZContainer{

    private boolean hash = false;
    private boolean recordInCache = false;
    private int maxDepth = Integer.MAX_VALUE;
    private String path = "C:\\Users\\Baptiste\\Desktop\\test";
    //private String path = "D:\\";
    private boolean thread = false;

    Analyzer analyzer;

    public TreePanel(Dimension dim, Analyzer analyzer) {
        super(dim);
        this.analyzer = analyzer;
        initPanel();
    }

    public void initPanel() {

        //analyzer.buildFileTree(path, thread, hash, recordInCache, maxDepth);

        //JTreeTable tree = new JTreeTable((TreeTableModel)analyzer.getTreeModel());
        //this.panel.add(tree, BorderLayout.CENTER);

        JTree tree = new JTree(analyzer.getRoot());
        this.panel.add(tree, BorderLayout.LINE_START);
        System.out.println("saluy");
    }
}
