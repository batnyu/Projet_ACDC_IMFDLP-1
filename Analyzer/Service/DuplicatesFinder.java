package Analyzer.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import Analyzer.Control.CacheManager;
import Analyzer.Control.ErrorManager;
import Analyzer.Model.FileNode;

/**
 * Class collecting duplicates files
 * 
 * @author Valentin Bourcier
 */
@SuppressWarnings("rawtypes")
public class DuplicatesFinder implements FileVisitor<Path>, Callable
{

    private Map<String, List<File>> duplicates;
    private Filter filter;
    private final Path rootPath;
    private CacheManager cache = CacheManager.getInstance();

    /**
     * Builder of the finder
     * @param rootPath Path of the root file
     * @param filter Filter instance checking duplicates files validity
     */
    @SuppressWarnings("hiding")
    public DuplicatesFinder(Path rootPath, Filter filter)
    {
        this.filter = filter;
        this.rootPath = rootPath;
        duplicates = new ConcurrentHashMap<String, List<File>>();
    }

    /**
     * Inherited from FileVisitor
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (!Files.isReadable(dir))
        {
            System.out.println("Erreur d'accès au dossier: " + dir.toString());
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Inherited from FileVisitor
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    /**
     * Inherited from FileVisitor
     */
    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
        if (Files.isReadable(path))
        {
            if (filter.accept(path.toFile()))
            {
                FileNode element = new FileNode(path.toString());
                if (!cache.contains(element.getAbsolutePath()))
                {
                    cache.add(element);
                }
                else
                {
                    element = cache.getMoreRecent(element.getAbsolutePath());
                }
                String hash = element.getHash();
                if (hash != null)
                {
                    List<File> files = duplicates.get(hash);
                    if (files != null)
                    {
                        files.add(element);
                    }
                    else
                    {
                        ArrayList<File> list = new ArrayList<File>();
                        list.add(element);
                        duplicates.put(hash, list);
                    }
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
    public Map<String, List<File>> call() {
        try
        {
            Files.walkFileTree(rootPath, this);
        }
        catch (IOException error)
        {
            System.out.println("Error while parsing files");
            ErrorManager.throwError(error);
            return null;
        }
        for (String hash : duplicates.keySet())
        {
            if (duplicates.get(hash).size() <= 1)
            {
                duplicates.remove(hash);
            }
        }
        return duplicates;
    }

}
