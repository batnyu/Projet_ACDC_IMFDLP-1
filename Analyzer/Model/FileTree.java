package Analyzer.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import Analyzer.Control.CacheManager;
import Analyzer.Control.ErrorManager;
import Analyzer.Control.SystemListener;
import Analyzer.Control.ThreadManager;
import Analyzer.Service.AlphanumComparator;
import Analyzer.Service.Analyzer;
import Analyzer.Service.DuplicatesFinder;
import Analyzer.Service.Filter;

/**
 * Class which represent the file system Tree.
 *
 * @author valentin
 */
public class FileTree extends Analyzer {

    private static AlphanumComparator comparator = new AlphanumComparator();

    private DefaultMutableTreeNode root;
    private CacheManager cache;

    /**
     * Tree builder
     */
    public FileTree() {
        cache = CacheManager.getInstance();
        root = new DefaultMutableTreeNode();
    }

    /**
     * Method allowing to get the tree root
     *
     * @return A DefaultMutableTreeNode corresponding to the tree root
     */
    @Override
    public DefaultMutableTreeNode getRoot() {
        return root;
    }

    /**
     * Method returning the FileNode contained on the indicated DefaultMutableTreeNode
     *
     * @param path A string representation of the node path in the tree
     * @return FileNode contained by the node in argument
     */
    @Override
    public FileNode getFileNode(String path) {
        return getFileNode(getChildByPath(path));
    }

    /**
     * Method returning the FileNode contained on the indicated DefaultMutableTreeNode
     *
     * @param node A DefaultMutableTreeNode containing a FileNode
     * @return FileNode contained by the node in argument
     */
    @Override
    public synchronized FileNode getFileNode(DefaultMutableTreeNode node) {
        return (FileNode) node.getUserObject();
    }

    /**
     * Method returning the tree depth
     *
     * @return An Integer representing the max depth on the tree
     */
    @Override
    public int getDepth() {
        return root.getDepth();
    }

    /**
     * Method getting a node from his path on the tree
     * Exemple: if root path is: "/home/", path could be "/session/.../". Path "/session/" is not valid, use getRoot()
     *
     * @param path The string representation of the node path searched
     * @return The node in tree corresponding to the path.
     */
    @Override
    public DefaultMutableTreeNode getChildByPath(String path) {
        DefaultMutableTreeNode node = root;
        @SuppressWarnings("unchecked")
        Enumeration<DefaultMutableTreeNode> en = node.preorderEnumeration();
        String[] nodePath = path.split("\\" + File.separator);
        String rootName = ((FileNode) root.getUserObject()).getName();
        String pathRoot = nodePath[0].equals("") ? nodePath[1] : nodePath[0];
        if (!rootName.equals(pathRoot)) {
            throw new IllegalArgumentException("Child path should begin on tree root.");
        }
        int cpt = nodePath[0].equals("") ? 1 : 0;
        while (en.hasMoreElements() && cpt < nodePath.length) {
            DefaultMutableTreeNode tmp = en.nextElement();
            TreeNode[] tmpParent = null;
            String parentPath = "";
            if (!tmp.isRoot()) {
                tmpParent = ((DefaultMutableTreeNode) tmp.getParent()).getPath();
                parentPath = tmpParent[tmpParent.length - 1].toString();
            }
            TreeNode[] tmpPath = tmp.getPath();
            String name = tmpPath[tmpPath.length - 1].toString();

            if (!tmp.isLeaf()) {
                if (name.equals(nodePath[cpt])) {
                    node = tmp;
                    cpt++;
                }
            } else if (cpt == nodePath.length - 1 && name.equals(nodePath[cpt])) {
                if (!tmp.isRoot() && parentPath.equals(nodePath[cpt - 1])) {
                    node = tmp;
                    cpt++;
                }

            }
        }
        if (rootName.equals(pathRoot) && node.equals(root) || cpt < nodePath.length - 1) {
            throw new IllegalArgumentException("Path cannot be verified by the tree.");
        }
        return node;
    }

    /**
     * Method allowing to get the weight of a tree/sub-tree, from a node path
     *
     * @return The number of Bytes reprenseting the weight of the node.
     */
    @Override
    public long getWeight(String path) {
        return getWeight(getChildByPath(path));
    }

    /**
     * Method getting the weight of a tree/sub-tree
     *
     * @return The number of Bytes reprenseting the weight of the node.
     */
    @Override
    public long getWeight(DefaultMutableTreeNode node) {
        long size = 0;
        @SuppressWarnings("unchecked")
        Enumeration<DefaultMutableTreeNode> en = node.preorderEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode next = en.nextElement();
            FileNode file = (FileNode) next.getUserObject();
//            if (file.isFile()) {
//                size += file.length();
//            }
            boolean vIsWindows = System.getProperty("os.name").toLowerCase().contains("windows");
            if (file.isFile() || (file.isDirectory() && !vIsWindows))
            {
                size += file.length();
            }
        }
        return size;
    }

    @Override
    public void setWeight(DefaultMutableTreeNode node) {
        @SuppressWarnings("unchecked")
        Enumeration<DefaultMutableTreeNode> en = node.breadthFirstEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode next = en.nextElement();
            FileNode file = (FileNode) next.getUserObject();
            if (file.isDirectory()) {
                long size = getWeight(next);
                file.setSize(size);
            } else {
                file.setSize(file.length());
            }
        }
    }

    /**
     * Method deleting empty folders in file tree
     */
    @Override
    @SuppressWarnings("unchecked")
    public void cleanEmptyFolders() {
        Enumeration<DefaultMutableTreeNode> en = root.breadthFirstEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = en.nextElement();
            if (node.getUserObject() instanceof File) {
                File file = (File) node.getUserObject();
                if (file.isDirectory() && node.isLeaf()) {
                    node.removeAllChildren();
                    node.removeFromParent();
                    en = root.breadthFirstEnumeration();
                }
            }

        }
    }

    /**
     * String representation of the file tree
     */
    @SuppressWarnings("unchecked")
    @Override
    public String toString() {
        Enumeration<DefaultMutableTreeNode> en = root.preorderEnumeration();
        String tree = "";
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = en.nextElement();
            TreeNode[] path = node.getPath();
            tree = tree + (node.isLeaf() ? "  - " : "+ ") + path[path.length - 1] + "\n";
        }
        return tree;
    }

    /**
     * Method returning duplicates files in a path
     *
     * @param filter Filter used to check files
     * @param path   String representation of the path on which you want to ckeck duplicates
     * @return Return list of duplicates indexed by hash in a map.
     */
    @Override
    public Map<String, List<File>> getDuplicates(String path, Filter filter) {
        unserializeCache();
        DuplicatesFinder finder = new DuplicatesFinder(Paths.get(path), filter);
        @SuppressWarnings("unchecked")
        Future<Map<String, List<File>>> duplicated = ThreadManager.getThread().submit(finder);
        try {
            serializeCache();
            return duplicated.get();
        } catch (Exception error) {
            error.printStackTrace();
            System.out.println("Error while getting duplicates");
            ErrorManager.throwError(error);
        }
        return null;
    }

    /**
     * Method allowing to check duplicates from a DefaultMutableTreeNode
     *
     * @param filter Filter used to check files
     * @param node   Node on which you want to ckeck duplicates
     * @return Return list of duplicates indexed by hash in a map.
     */
    @Override
    public Map<String, List<File>> getDuplicates(DefaultMutableTreeNode node, Filter filter) {
        return getDuplicates(getFileNode(node).getAbsolutePath(), filter);
    }

    /**
     * Method returning a TreeModel representation of the current FileTree
     *
     * @return A TreeModel
     */
    @Override
    public TreeModel getTreeModel() {
        return new DefaultTreeModel(root);
    }

    /**
     * Builder of the File Tree
     *
     * @param rootPath      String representation of the root path
     * @param filter        Filter used to check files
     * @param thread        Boolean, equals true for building the FileTree using threads (increase required resources)
     * @param hash          Boolean, equals true for hashing files on tree building, false either
     * @param recordInCache Boolean, equals true for saving tree files on cache.
     * @return The DefaultMutableTreeNode root of the Tree in a thread result object.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Future<DefaultMutableTreeNode> buildFileTree(String rootPath,
                                                        Filter filter,
                                                        Boolean thread,
                                                        Boolean hash,
                                                        Boolean recordInCache,
                                                        int maxDepth) {
        Path path = Paths.get(rootPath);
        unserializeCache();
        root = new DefaultMutableTreeNode(new FileNode(rootPath));
        FileTreeFactory factory = new FileTreeFactory(path, root, filter, thread, hash, recordInCache, maxDepth);
        Future<DefaultMutableTreeNode> result = ThreadManager.getThread().submit(factory);
        try {
            root = result.get();

        } catch (Exception error) {
            error.printStackTrace();
            System.out.println("FileTree building failed");
            ErrorManager.throwError(error);
        }
        if (filter.isActive()) {
            cleanEmptyFolders();
        }
        if (recordInCache) {
            serializeCache();
        }

        return result;
    }

    /**
     * Default tree builder
     *
     * @param rootPath      String representation of the root path
     * @param thread        Boolean, equals true for building the FileTree using threads (increase required resources)
     * @param hash          Boolean, equals true for hashing files on tree building, false either
     * @param recordInCache Boolean, equals true for saving tree files on cache.
     * @return The DefaultMutableTreeNode root of the Tree in a thread result object.
     */
    @Override
    public Future<DefaultMutableTreeNode> buildFileTree(String rootPath, Boolean thread, Boolean hash, Boolean recordInCache, int maxDepth) {
        return buildFileTree(rootPath, new Filter(), thread, hash, recordInCache, maxDepth);
    }

    /**
     * Default tree builder
     *
     * @param rootPath String representation of the root path.
     * @return The DefaultMutableTreeNode root of the Tree in a thread result object.
     */
    @Override
    public Future<DefaultMutableTreeNode> buildDefaultFileTree(String rootPath) {
        return buildFileTree(rootPath, new Filter(), false, false, true, 0);
    }

    /**
     * Method used to launch the semi real-time listening of the system (checking all N seconds)
     *
     * @param millisRefresh Refreshing delay in milli-seconds
     */
    @Override
    public void listenSystemChanges(int millisRefresh) {
        SystemListener.SYSTEM_LISTENER.registerTree(this);
        SystemListener.SYSTEM_LISTENER.setDelay(millisRefresh);
        ThreadManager.getThread().submit(SystemListener.SYSTEM_LISTENER);
    }

    /**
     * Method removing a Node in file tree
     *
     * @param path Path of the node
     */
    @Override
    public void deleteNode(String path) {
        deleteNode(getChildByPath(path));
    }

    /**
     * Method removing a Node in file tree
     *
     * @param node DefaultMutableTreeNode instance to delete
     */
    @Override
    public void deleteNode(DefaultMutableTreeNode node) {
        @SuppressWarnings("unchecked")
        Enumeration<DefaultMutableTreeNode> en = node.preorderEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode nextNode = en.nextElement();
            if (nextNode.isLeaf()) {
                cache.remove(getFileNode(node).getAbsolutePath());
            }
        }
        node.removeAllChildren();
        node.removeFromParent();
        File file = getFileNode(node);
        if (file.exists()) {
            try {
                delete(file);
            } catch (IOException error) {
                error.printStackTrace();
                ErrorManager.throwError(error);
            }

        }
    }

    /**
     * Method used to delete a file from the system
     *
     * @param file The File to delete
     */
    public static void delete(File file) throws IOException {
        if (file.isDirectory()) {
            for (File childFile : file.listFiles()) {
                if (childFile.isDirectory()) {
                    delete(childFile);
                } else {
                    if (!childFile.delete()) {
                        throw new IOException();
                    }
                }
            }
        }

        if (!file.delete()) {
            throw new IOException();
        }
    }

/*    public void sortTree(TreeModel treeModel) {
        treeModel.reload
        treeModel.reload(sort(getRoot()));
    }*/

    public static DefaultMutableTreeNode sortFolderFirst(DefaultMutableTreeNode node) {
        //put folders first - normal on Windows and some flavors of Linux but not on Mac OS X.
        for (int i = 0; i < node.getChildCount() - 1; i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            for (int j = i + 1; j <= node.getChildCount() - 1; j++) {
                DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) node.getChildAt(j);

                if (!prevNode.isLeaf() && child.isLeaf()) {
                    node.insert(child, j);
                    node.insert(prevNode, i);
                }
            }
        }
        return node;
    }

    public static DefaultMutableTreeNode sortAlphabetically(DefaultMutableTreeNode node, boolean desc) {

        //sort alphabetically
        for (int i = 0; i < node.getChildCount() - 1; i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            String nt = ((FileNode) child.getUserObject()).getName().toLowerCase();
            for (int j = i + 1; j <= node.getChildCount() - 1; j++) {
                DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) node.getChildAt(j);
                String np = ((FileNode) prevNode.getUserObject()).getName().toLowerCase();
                System.out.println(nt + " " + np + " -> " + comparator.compare(np, nt));
                if (desc) {
                    if (comparator.compare(np, nt) > 0) {
                        node.insert(child, j);
                        node.insert(prevNode, i);
                    }
                } else {
                    if (comparator.compare(nt, np) > 0) {
                        node.insert(child, j);
                        node.insert(prevNode, i);
                    }
                }
            }
            if (child.getChildCount() > 0) {
                sortAlphabetically(child, desc);
            }
        }

        return node;
    }

    public static DefaultMutableTreeNode sortByWeight(DefaultMutableTreeNode node, boolean desc) {
        display(node);
        //sort by weight
        for (int i = 0; i < node.getChildCount() - 1; i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            long nt = ((FileNode) child.getUserObject()).getSize();
            for (int j = i + 1; j <= node.getChildCount() - 1; j++) {
                DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) node.getChildAt(j);
                long np = ((FileNode) prevNode.getUserObject()).getSize();
                System.out.println("Comparing " + ((FileNode) child.getUserObject()).getName() + " and " + ((FileNode) prevNode.getUserObject()).getName());
                if (desc) {
                    if(np > nt){
                        node.insert(child,j);
                        System.out.println("-> Interverting " + ((FileNode) child.getUserObject()).getName() + " and " + ((FileNode) prevNode.getUserObject()).getName());
                        display(node);
                        break;
                        //node.insert(prevNode, i);

                    }
                } else {
                    if(nt > np){
                        node.insert(child,j);
                        node.insert(prevNode, i);
                    }
                }
            }
            if (child.getChildCount() > 0) {
                node = sortByWeight(child, desc);
            }
        }
        return node;
    }

    public static void display(DefaultMutableTreeNode root) {
        System.out.println("\n--- TREE ---\n");
        String result = "";
        Enumeration<DefaultMutableTreeNode> en = root.preorderEnumeration();
        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = en.nextElement();
            String nodeValue = node.getUserObject().toString();
            boolean isDirectory = ((FileNode)node.getUserObject()).isDirectory();
            String indent = "";
            while (node.getParent() != null) {
                indent += "    ";
                node = (DefaultMutableTreeNode)node.getParent();
            }
            if(isDirectory){
                indent += "+";
            } else {
                indent += "-";
            }
            result += indent + nodeValue + "\n";
        }
        System.out.println(result);
    }


}
