package Analyzer.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import Analyzer.Control.CacheManager;
import Analyzer.Control.ErrorHandler;
import Analyzer.Control.ErrorManager;
import Analyzer.Control.FileTreeListener;
import Analyzer.Control.SystemListener;
import Analyzer.Model.FileNode;
import Analyzer.Model.InfoNode;

/**
 * Analyser services 
 * 
 * @author Valentin Bourcier
 */
public abstract class Analyzer
{

    /**
     * Default tree builder
     * @param rootPath String representation of the root path.
     * @return The DefaultMutableTreeNode root of the Tree in a thread result object.
     */
    public abstract Future<DefaultMutableTreeNode> buildDefaultFileTree(String rootPath);

    /**
     * Builder of the File Tree
     * @param rootPath String representation of the root path
     * @param filter Filter used to check files
     * @param thread Boolean, equals true for building the FileTree using threads (increase required resources)
     * @param hash Boolean, equals true for hashing files on tree building, false either
     * @param recordInCache Boolean, equals true for saving tree files on cache.
     * @param maxDepth Integer equivalent to the max depth of the tree that you want to build
     * @return The DefaultMutableTreeNode root of the Tree in a thread result object.
     */
    public abstract Future<DefaultMutableTreeNode> buildFileTree(String rootPath,
                                                                 Filter filter,
                                                                 Boolean thread,
                                                                 Boolean hash,
                                                                 Boolean recordInCache,
                                                                 int maxDepth);

    /**
     * Builder setting the filter by default
     * @param rootPath String representation of the root path
     * @param thread Boolean, equals true for building the FileTree using threads (increase required resources)
     * @param hash Boolean, equals true for hashing files on tree building, false either
     * @param recordInCache Boolean, equals true for saving tree files on cache.
     * @param maxDepth Integer equivalent to the max depth of the tree that you want to build
     * @return The DefaultMutableTreeNode root of the Tree in a thread result object.
     */
    public abstract Future<DefaultMutableTreeNode> buildFileTree(String rootPath, Boolean thread, Boolean hash, Boolean recordInCache, int maxDepth);

    /**
     * Method returning duplicates files in a path
     * @param filter Filter used to check files
     * @param path String representation of the path on which you want to ckeck duplicates
     * @return Return list of duplicates indexed by hash in a map.
     */
    public abstract Map<String, List<File>> getDuplicates(String path, Filter filter);

    /**
     * Method allowing to check duplicates from a DefaultMutableTreeNode
     * @param filter Filter used to check files
     * @param node Node on which you want to ckeck duplicates
     * @return Return list of duplicates indexed by hash in a map.
     */
    public abstract Map<String, List<File>> getDuplicates(DefaultMutableTreeNode node, Filter filter);

    /**
     * Method returning a TreeModel representation of the current FileTree
     * @return A TreeModel
     */
    public abstract TreeModel getTreeModel();

    /**
     * Method deleting cache from system and reseting cache in JVM
     */
    public void cleanCache()
    {
        CacheManager.getInstance().clean();
    }

    /**
     * Method which serialize the cache 
     */
    public void serializeCache()
    {
        CacheManager.getInstance().serialize();
    }

    /**
     * Method unserializing the cache
     */
    public void unserializeCache()
    {
        CacheManager.getInstance().unserialize();
    }

    /**
     * Method used to launch the semi real-time listening of the system (checking all N seconds)
     * @param millisRefresh Refreshing delay in milli-seconds
     */
    public abstract void listenSystemChanges(int millisRefresh);
    
    /**
     * Method used to add an error handler on the system
     * @param handler ErrorHandler object
     */
    public void addErrorHandler(ErrorHandler handler) {
    	ErrorManager.addHandler(handler);
    }

    /**
     * Method to add a listener on FileTree updates
     * @param listener Instance of FileTreeListener
     */
    public void addFileTreeListener(FileTreeListener listener)
    {
        SystemListener.SYSTEM_LISTENER.addFileTreeListener(listener);
    }

    /**
     * Method allowing to get the weight of a tree/sub-tree, from a node path
     * @param path String representation of the tree root path 
     * @return The number of Bytes representing the weight of the node.
     */
    public abstract long getWeight(String path);

    /**
     * Method getting the weight of a tree/sub-tree
     * @param node Root of the tree that we would like to get he weight
     * @return The number of Bytes representing the weight of the node.
     */
    public abstract long getWeight(DefaultMutableTreeNode node);

    public abstract InfoNode getInfoNode(DefaultMutableTreeNode node);

    public abstract void setInfoNode(DefaultMutableTreeNode node);

    /**
     * Method returning the FileNode contained on the indicated DefaultMutableTreeNode
     * @param path A string representation of the node path in the tree
     * @return FileNode contained by the node in argument
     */
    public abstract FileNode getFileNode(String path);

    /** Method returning the FileNode contained on the indicated DefaultMutableTreeNode
     * @param node A DefaultMutableTreeNode containing a FileNode
     * @return FileNode contained by the node in argument
     */
    public abstract FileNode getFileNode(DefaultMutableTreeNode node);

    /**
     * Method removing a Node in file tree
     * @param path Path of the node
     */
    public abstract void deleteNode(String path);

    /**
     * Method removing a Node in file tree
     * @param node DefaultMutableTreeNode instance to delete
     */
    public abstract void deleteNode(DefaultMutableTreeNode node);

    /**
     * Method returning the tree depth
     * @return An Integer representing the max depth on the tree
     */
    public abstract int getDepth();

    /**
     * Method deleting empty folders in file tree
     */
    public abstract void cleanEmptyFolders();

    /**
     * Method getting a node from his path on the tree
     * Exemple: if root path is: "/home/", path could be "/session/.../". Path "/session/" is not valid, use getRoot()
     * @param path The string representation of the node path searched
     * @return The node in tree corresponding to the path.
     */
    public abstract DefaultMutableTreeNode getChildByPath(String path);

    /**
     * Method allowing to get the tree root
     * @return A DefaultMutableTreeNode corresponding to the tree root
     */
    public abstract DefaultMutableTreeNode getRoot();

}
