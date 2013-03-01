package peer.file;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Description of FileFinder class:<br> This class is used to search a given
 * location (path, for example "C:/Shared Folder/"), to find all files that
 * exist there and should be later shared. In this implementation, only files in
 * the specified folder are returned, not files that exist in folders in the
 * given path. For example, if "C:/Shared Folder/" is the given path then only
 * files in this folder will be shared, not files in upper or lower depth of the
 * given location.
 *
 * @author Georgios Paralykidis
 * @author Filippia Zikou
 *
 * @version 1.00a (Beta)
 *
 * @since Java 1.6 - 03 December 2012.
 */
public class FileFinder {

    public FileFinder() {
    }

    /**
     * Finds all the files inside the specified location
     * @param location The location in which files will be searched from.
     * @return List<SharedFile> - A list of SharedFiles made by the files that
     * existed in the given directory.
     */
    public List<SharedFile> findNow(String location) {
        //"fold" is the folder in which are the files to be shared:
        File fold = new File(location);
        //Get the files and folders in "fold" and put them in a list 
        File[] listOfFiles = fold.listFiles();
        File[] listOfFiles2;
        listOfFiles2 = new File[99];

        List<SharedFile> sharedFiles;
        sharedFiles = new ArrayList<SharedFile>();
        for (int i = 0; i < listOfFiles.length; i++) {
            //From files and folders choose only files:
            if (listOfFiles[i].isFile()) {
                SharedFile sharedFile = new SharedFile(listOfFiles[i].getName(), listOfFiles[i].length());
                sharedFiles.add(sharedFile);
            }
        }

        return sharedFiles;
    }
}
