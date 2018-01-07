package Analyzer.Model;

public class InfoNode {

    private long size;
    private long numberFiles;
    private long numberFolders;

    public InfoNode(long size, long numberFiles, long numberFolders) {
        this.size = size;
        this.numberFiles = numberFiles;
        this.numberFolders = numberFolders;
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
}
