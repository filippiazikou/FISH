package peer.file;

import java.io.Serializable;
/**
 * 
 * @author filippia
 */
public class SharedFile implements Serializable {
    private String fileName;
    private long size;

    /**
     * SharedFile abstraction constructor. Creates a Shared file
     * object with the specified file name and size
     * @param fileName Name of the file to be used in the SharedFile object
     * @param size Size of the file to be used in the SharedFile object
     */
    public SharedFile(String fileName, long size) {
        this.fileName = fileName;
        this.size = size;
    }

    /**
     * Get the name of the file in the SharedFile object
     * @return String containing the name of the file in the SharedFile object
     */
    public String getName() {
        return fileName;
    }
    
    /**
     * Get the size of the file in the SharedFile object
     * @return long indicating the size of the file in the SharedFile object
     */
    public long getSize() {
        return size;
    }
    
    /**
     * Checks if a File object represents a file or a folder
     * @return true if the File object is a file or false if the File object is a folder
     */
    public boolean isFile(){
        if (this.getName().isEmpty()){
            return false;
        }else
            return true;     
    }
}