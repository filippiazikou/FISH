package file;

import java.io.Serializable;

/**
 * Description of SharedFile class:<br> This class is used to represent in code
 * level the files that a client in Fish Project can share from a local folder.
 * It contains all the necessary details like name, size, extension etc.
 *
 * @author Georgios Paralykidis
 * @author Filippia Zikou
 * 
 * @version 1.00a (Beta)
 * @
 * @since Java 1.6 - 03 December 2012.
 * 
 * @param ID The unique ID of the file.
 * @param fileName The name of the file without the extension.
 * @param extension The extension of the file starting with "." (if file has
 * one).
 * @param size The size of the file in Bytes.
 * @param owner The name with which the client sharing the file registered when
 * logging into the server.
 */
public class SharedFile implements Serializable {

    /**
     * Description of SharedFile class:
     */
    private int ID;
    private String fileName;
    private String extension;
    private long size;
    private String owner;

    /**
     * @param ID The unique ID of the file.
     * @param fileName The name of the file without the extension.
     * @param extension The extension of the file starting with "." (if file has
     * one ).
     * @param size The size of the file in Bytes .
     * @param owner The name with which the client sharing the file registered
     * when logging into the server.
     */
    public SharedFile(int ID, String fileName, String extension, long size, String owner) {
        this.ID = ID;
        this.fileName = fileName;
        this.extension = extension;
        this.size = size;
        this.owner = owner;
    }

    /**
     * This method returns the ID of a SharedFile. If no ID exists for that file
     * it returns null.
     *
     * @return ID - The unique ID of the file (e.g. 5), null if does not exist.
     */
    public int getID() {
        return ID;
    }

    /**
     * This method returns the fileName of the SharedFile. If no fileName exists
     * for that file it returns null.
     *
     * @return fileName - The name of the file without the extension (e.g.
     * "document"), null if does not exist.
     */
    public String getName() {
        return fileName;
    }

    /**
     * This method returns the extention of the SharedFile. If no extension
     * exists for that file it returns null.
     *
     * @return extension - The extension (e.g.: ".txt") of the file. null if
     * does not exist.
     */
    public String getExtension() {
        return extension;
    }

    /**
     * This method returns the size of the SharedFile. If no size exists for
     * that file it returns null.
     *
     * @return size - The size of the file (e.g. 500), null if does not exist.
     */
    public long getSize() {
        return size;
    }

    /**
     * This method returns the owner of the SharedFile. If no owner exists for
     * that file it returns null.
     *
     * @return owner - The name with which the client sharing the file
     * registered when logging into the server (e.g. "George"), null if does not
     * exist.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * This method returns the "true" if target is a file. If not returns false
     *
     * @return true - If target is file. Else returns false.
     */
    public boolean isFile() {
        if (this.getName().isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
}