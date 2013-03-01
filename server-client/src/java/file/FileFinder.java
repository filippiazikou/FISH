package file;

import java.io.File;

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
    /**
     * Description of FileFinder class:
     */
    public FileFinder() {
    }

    /**
     * @param location The location in which files will be searched from.
     * @return SharedFile[] - A list of SharedFiles made by the files that
     * existed in the given directory.
     */
    public static SharedFile[] findNow(String location) {
        //"fold" is the folder in which are the files to be shared:
        File fold = new File(location);
        //Get the files and folders in "fold" and put them in a list 
        File[] listOfFiles = fold.listFiles();
        //Each client can share up to 99 files.
        File[] listOfFiles2 = new File[99];

        int i;
        for (i = 0; i < listOfFiles.length; i++) {
            //From files and folders choose only files:
            if (listOfFiles[i].isFile()) {
                //And put them in a second list:
                listOfFiles2[i] = listOfFiles[i];
            }
        }

        // Now from the second list we create ShareFile objects, that represent
        // real file objects in code-level representations.
        i--;
        SharedFile[] sharedFiles = new SharedFile[listOfFiles.length];
        for (int y = 0; y<=i; y++) {
            String[] splittedText = listOfFiles2[y].getName().split("\\.",2);
            long size = listOfFiles2[y].length();
            
            SharedFile sharedFile = new SharedFile(0, splittedText[0], splittedText[1], size, "");
            sharedFiles[y] = sharedFile;
        }
        //Return the second list:
        return sharedFiles;
    }
}