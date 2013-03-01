package client;

import file.SharedFile;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultList implements Serializable {

    private transient ResultSet s;
    List<SharedFile> list = new ArrayList<SharedFile>();

    public ResultList(ResultSet s) throws SQLException {
        this.s = s;
        execute();
    }

    private List execute() throws SQLException {
        while (s.next()) { // process results one row at a time.
            int ID = s.getInt("ID");
            String fileName = s.getString("FILENAME");
            String extension = s.getString("EXTENSION");
            int size = s.getInt("SIZE");
            String owner = s.getString("OWNER");
            SharedFile sharedFile = new SharedFile(ID, fileName, extension, size, owner);
            list.add(sharedFile);
        }
        return list;
    }

    public SharedFile get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }
}