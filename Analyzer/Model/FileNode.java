package Analyzer.Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import Analyzer.Control.ErrorManager;

/**
 * Class defining the FileNode object in FileTree
 *
 * @author Valentin Bourcier
 */
public class FileNode extends File implements Serializable {

    private static final long serialVersionUID = 1L;
    private String hash;
    // Date of creation of the current FileNode
    public final long INSTANCE_TIME = System.currentTimeMillis();

    private long size;
    private long numberFiles;
    private long numberFolders;

    /**
     * FileNode builder
     *
     * @param path String representation of the file absolute path
     */
    public FileNode(String path) {
        super(path);
        hash = null;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getNumberFiles() {
        return numberFiles;
    }

    public void setNumberFiles(long numberFiles) {
        this.numberFiles = numberFiles;
    }

    public long getNumberFolders() {
        return numberFolders;
    }

    public void setNumberFolders(long numberFolders) {
        this.numberFolders = numberFolders;
    }

    /**
     * Method calculating hash of a file
     *
     * @param algorithme Digest algorithme used: "MD5", "SHA1"...
     * @return The hash string of current file
     */
    public String hash(String algorithme) {
        if (isFile() && canRead()) {
            DigestInputStream digest = null;
            try {
                MessageDigest md = MessageDigest.getInstance(algorithme);
                digest = new DigestInputStream(new FileInputStream(getAbsolutePath()), md);

                byte[] byteArray = new byte[8192];
                int bytesCount = 0;

                while (digest.read(byteArray) > 0) {
                    md.update(byteArray, 0, bytesCount);
                }
                digest.close();

                byte[] bytes = md.digest();

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < bytes.length; i++) {
                    sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
                }

                hash = sb.toString();

            } catch (NoSuchAlgorithmException error) {
                System.out.println("Hashage impossible, algorithme non supporté.");
                ErrorManager.throwError(error);
            } catch (FileNotFoundException error) {
                error.printStackTrace();
                System.out.println("Hashage impossible, fichier non trouvé.");
                ErrorManager.throwError(error);
            } catch (IOException error) {
                System.out.println("Hashage interrompu, erreur de lecture.");
                ErrorManager.throwError(error);
            }

            return hash;
        }
        return null;
    }

    /**
     * Method launching hash if not already calculated
     *
     * @return The hash string of current file
     */
    public String getHash() {
        if (hash == null) {
            hash("MD5");
        }
        return hash;
    }

    /**
     * String representation of the file
     */
    @Override
    public String toString() {
        return getName();
    }

}
