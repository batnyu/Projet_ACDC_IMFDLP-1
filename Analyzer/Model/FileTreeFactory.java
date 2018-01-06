package Analyzer.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.swing.tree.DefaultMutableTreeNode;

import Analyzer.Control.CacheManager;
import Analyzer.Control.ErrorManager;
import Analyzer.Control.ThreadManager;
import Analyzer.Service.Filter;

/**
 * Class building a file tree
 *
 * @author Valentin Bourcier
 */
@SuppressWarnings("rawtypes")
public class FileTreeFactory implements FileVisitor<Path>, Callable {

    private DefaultMutableTreeNode root;
    private DefaultMutableTreeNode currentNode;
    private Filter filter;
    private final Path rootPath;
    private CacheManager cache;
    private Boolean hash;
    private Boolean recordInCache = true;
    private int maxDepth;
    private List<Future<DefaultMutableTreeNode>> results;
    private boolean thread = true;

    private Deque<Long> dirSizeStack = new ArrayDeque<>();

    /**
     * Factory initializer
     *
     * @param rootPath      Path of the root file
     * @param root          The root object of the future FileTree
     * @param filter        The Filter used to restrict files in the Tree
     * @param thread        Boolean, equals true for building the FileTree using threads (increase required resources)
     * @param hash          Boolean equals to True if the factory should hash files
     * @param recordInCache Boolean equals to True if the factory should use the cache
     * @param maxDepth      Integer equivalent to the max depth of the tree that you want to build
     */
    @SuppressWarnings("hiding")
    public FileTreeFactory(Path rootPath, DefaultMutableTreeNode root, Filter filter, Boolean thread, Boolean hash, Boolean recordInCache,
                           int maxDepth) {
        this.filter = filter;
        this.root = root;
        currentNode = this.root;
        this.rootPath = rootPath;
        cache = CacheManager.getInstance();
        this.hash = hash;
        this.recordInCache = recordInCache;
        this.maxDepth = maxDepth;
        results = new ArrayList<Future<DefaultMutableTreeNode>>();
        this.thread = thread;
    }

    /**
     * Inherited from FileVisitor
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (Files.isReadable(dir)) {

            if (thread) {
                if (!dir.equals(FileTreeFactory.this.rootPath)) {
                    FileTreeFactory factory = new FileTreeFactory(dir, root, filter, thread, hash, recordInCache, maxDepth);
                    Future<DefaultMutableTreeNode> result = ThreadManager.getThread().submit(factory);
                    results.add(result);
                    return FileVisitResult.SKIP_SUBTREE;
                } else {
                    root = new DefaultMutableTreeNode(new FileNode(dir.toString()));
                    currentNode = root;
                }
            } else {
                //dirSizeStack.push((long) 0);

                if (!dir.equals(FileTreeFactory.this.rootPath)) {
                    DefaultMutableTreeNode directory = new DefaultMutableTreeNode(new FileNode(dir.toString()));
                    currentNode.add(directory);
                    currentNode = directory;
                }

            }

        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Inherited from FileVisitor
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        if (!thread) {
            //long size = dirSizeStack.pop();

            //((FileNode) currentNode.getUserObject()).setSize(size);

//            if (!dirSizeStack.isEmpty()) // add this dir size to parent's size
//                dirSizeStack.push(dirSizeStack.pop() + size);

            currentNode = (DefaultMutableTreeNode) currentNode.getParent();
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Inherited from FileVisitor
     */
    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
        if (Files.isReadable(path)) {

            if (attrs.isRegularFile()) {
                File file = new File(path.toString());
                boolean isValid = !filter.isActive() || filter.accept(file);
                if (isValid) {
                    DefaultMutableTreeNode node;
                    if (recordInCache && cache.contains(path.toString())) {
                        node = new DefaultMutableTreeNode(cache.getMoreRecent(path.toString()));
                    } else {
                        FileNode fileNode = new FileNode(path.toString());
                        if (hash) {
                            fileNode.hash("MD5");
                        }

                        //Added by tisba
                        //fileNode.setSize(attrs.size());

                        node = new DefaultMutableTreeNode(fileNode);
                        if (recordInCache && !cache.contains(path.toString())) {
                            cache.add(fileNode);
                        }
                    }
                    currentNode.add(node);
                    //dirSizeStack.push(dirSizeStack.pop() + attrs.size());
                }
            }
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Inherited from FileVisitor
     */
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        System.out.println("File reading failed for: " + exc.getMessage());
        ErrorManager.throwError(exc);
        return FileVisitResult.CONTINUE;
    }

    /**
     * Method launching the research
     */
    @Override
    public DefaultMutableTreeNode call() throws Exception {
        try {
            if (maxDepth > 0) {
                Files.walkFileTree(rootPath, EnumSet.noneOf(FileVisitOption.class), maxDepth, this);
            } else {
                Files.walkFileTree(rootPath, this);
            }

        } catch (IOException error) {
            error.printStackTrace();
            System.out.println("Error while creating FileTree");
            ErrorManager.throwError(error);
        }

        if (thread) {
            for (Future<DefaultMutableTreeNode> result : results) {
                root.add(result.get());
            }
            Thread.currentThread().interrupt();
        }

        return root;
    }

}