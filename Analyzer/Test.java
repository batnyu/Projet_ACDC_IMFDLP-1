package Analyzer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import Analyzer.Control.CacheManager;
import Analyzer.Control.ErrorManager;
import Analyzer.Model.FileNode;
import Analyzer.Model.FileTree;
import Analyzer.Service.Analyzer;
import Analyzer.Service.Filter;

/**
 * Class executing Unit tests
 * 
 *  @author Valentin Bourcier
 */
public class Test
{

    public static Analyzer analyzer = new FileTree();

    /**
     * Method allowing to delete the test tree when cases are executed.
     * @param file File object to delete
     * @throws IOException if file deleting failed
     */
    public static void delete(File file) throws IOException
    {
        for (File childFile : file.listFiles())
        {
            if (childFile.isDirectory())
            {
                delete(childFile);
            }
            else
            {
                if (!childFile.delete())
                {
                    throw new IOException();
                }
            }
        }
        file.delete();
    }

    /**
     * Method which build a tree for unit cases execution 
     */
    public static void buildUnitTestTree()
    {
        try
        {
            File dir = new File("tests");
            if (dir.exists() && dir.isDirectory())
            {
                delete(dir);
            }
            dir.mkdir();
            dir = new File("tests" + File.separator + "folder1");
            dir.mkdir();
            dir = new File("tests" + File.separator + "folder2");
            dir.mkdir();
            FileWriter file = new FileWriter(new File("tests" + File.separator + "folder1" + File.separator + "duplicated-file1"), true);
            file.write("duplicates1");
            file.close();
            file = new FileWriter(new File("tests" + File.separator + "folder1" + File.separator + "duplicated-file2"), true);
            file.write("duplicates2");
            file.close();

            file = new FileWriter(new File("tests" + File.separator + "folder2" + File.separator + "duplicated-file1"), true);
            file.write("duplicates1");
            file.close();
            file = new FileWriter(new File("tests" + File.separator + "folder2" + File.separator + "duplicated-file2"), true);
            file.write("duplicates2");
            file.close();
            dir = new File("tests" + File.separator + "folder2" + File.separator + "empty-folder");
            dir.mkdir();

        }
        catch (IOException io)
        {
            io.printStackTrace();
            ErrorManager.throwError(io);
        }
    }

    /**
     * Build the file tree without hashing or storing files. 
     */
    public static void testBuildFileTree()
    {
        System.out.println("#### Building the test tree ####\n");
        analyzer.buildFileTree("tests", false, false, false, 0);
        System.out.println("Depth of the tree (2): "+ analyzer.getDepth()+ "\n");
        System.out.println(analyzer);
    }

    /**
     * Collecting duplicates without filtering it
     */
    public static void testDuplicates()
    {
        System.out.println("\n#### Searching duplicates ####\n");
        Map<String, List<File>> dup = analyzer.getDuplicates("tests", new Filter());
        for (String hash : dup.keySet())
        {
            for (File file : dup.get(hash))
            {
                System.out.println(hash + " -> " + file.getAbsolutePath());
            }
        }
    }

    /**
     * Cleaning cache, then test serialization
     */
    public static void testCache()
    {

        System.out.println("\n#### Testing cache ####\n");

        System.out.println("Deleting cache if exists");
        analyzer.cleanCache();

        // Building a FileTree in order to put some files in cache
        System.out.println("Filling cache");
        analyzer.buildDefaultFileTree("tests");

        System.out.println("Cache is initialized ? -> " + Files.exists(Paths.get(CacheManager.LOCATION)));
    }

    /**
     * Cleaning cache, then test serialization
     */
    public static void testDeletion()
    {
        System.out.println("\n#### Testing deletion ####\n");
        analyzer.buildDefaultFileTree("tests");
        analyzer.deleteNode("tests" + File.separator + "folder1" + File.separator + "duplicated-file1");
        System.out.println("File correctly deleted ? -> " + !Files.exists(Paths.get("folder1" + File.separator + "duplicated-file1")));
    }

    /**
     * Cleaning cache, then test serialization
     */
    public static void testGetFileNodeAndWeight()
    {
        System.out.println("\n#### Testing getNode and getWeight ####\n");
        analyzer.buildDefaultFileTree("tests");
        DefaultMutableTreeNode root = analyzer.getRoot();
        FileNode fileRoot = analyzer.getFileNode(root);
        System.out.println("FileNode == fileRoot ? -> " + (fileRoot == root.getUserObject()));
        System.out.println("Tree weight (33) -> " + analyzer.getWeight(root));
    }

    /**
     * Deleting empty directories from File Tree 
     */
    public static void testCleanEmptyFolders()
    {
        System.out.println("\n#### Testing empty folders cleaning in FileTree ####\n");
        analyzer.buildDefaultFileTree("tests");
        System.out.println(analyzer);
        analyzer.cleanEmptyFolders();
        System.out.println(analyzer);
    }

    public static void launchTests()
    {
        Test.buildUnitTestTree();
        Test.testBuildFileTree();
        Test.testDuplicates();
        Test.testCache();
        Test.testDeletion();
        Test.testGetFileNodeAndWeight();
        Test.testCleanEmptyFolders();
        try
        {
            Test.delete(new File("tests"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
