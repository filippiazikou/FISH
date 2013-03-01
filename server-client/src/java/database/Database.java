package database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private String datasource;
    private String dbms;
    //Prepared SQL Statements:
    //For ONLINE_CLIENTS table:
    public PreparedStatement addOnlineClient, removeOnlineClient, getClientByName;
    //For FILES table;
    public PreparedStatement getAllFiles, getFilesByName, getFilesByExtention, getFilesByOwner;
    public PreparedStatement getFilesByMinimunSize, getFilesByMaximunSize, addFile, removeFile;
    public PreparedStatement removeAllFilesByClient, getFileByID;

    public Database(String dbms, String datasource, String username, String password) {
        this.dbms = dbms;
        this.datasource = datasource;
    }

    public Connection createNewConnection() throws ClassNotFoundException, SQLException {
        //Try to create a database.
        //Depending on the DBMS, use different connection Drivers:
        //Note that if a database already exists in the given location, the the connection uses the existing DB.
        //If the database does not exist, then a new with the given name is created
        if (dbms.equalsIgnoreCase("access")) {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            return DriverManager.getConnection("jdbc:odbc:" + datasource);
        } else if (dbms.equalsIgnoreCase("cloudscape")) {
            Class.forName("COM.cloudscape.core.RmiJdbcDriver");
            return DriverManager.getConnection(
                    "jdbc:cloudscape:rmi://localhost:1099/" + datasource + ";create=true;");
        } else if (dbms.equalsIgnoreCase("pointbase")) {
            Class.forName("com.pointbase.jdbc.jdbcUniversalDriver");
            return DriverManager.getConnection(
                    "jdbc:pointbase:server://localhost:9092/" + datasource + ",new", "PBPUBLIC", "PBPUBLIC");
        } else if (dbms.equalsIgnoreCase("derby")) {
            Class.forName("org.apache.derby.jdbc.ClientXADataSource");
            return DriverManager.getConnection(
                    "jdbc:derby://localhost:1527/" + datasource + ";create=true");
        } else if (dbms.equalsIgnoreCase("mysql")) {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/" + datasource, "root", "javajava");
        } else {
            return null;
        }
    }

    public void createDatasource(Connection connection, String tableName) throws ClassNotFoundException, SQLException {
        //Create a new conntection to a database with given name and DBMS:
        boolean exist = false;
        int tableNameColumn = 3;
        DatabaseMetaData dbm = connection.getMetaData();
        //Checks to see if the "ACCOUNT" table is already created.
        //If it exists, then does nothing and returns.
        for (ResultSet rs = dbm.getTables(null, null, null, null); rs.next();) {
            if (rs.getString(tableNameColumn).equals(tableName)) {
                exist = true;
                rs.close();
                break;
            }
        }
        //If it does not exist then it creates the table:
        if (!exist) {
            //Creates the new statement:
            Statement statement = connection.createStatement();
            //And executes it:
            if (tableName.equals("ONLINE_CLIENTS")) {
                statement.executeUpdate("CREATE TABLE " + tableName + " (USERNAME VARCHAR(32) PRIMARY KEY, IP VARCHAR(32), PORT INTEGER)");
            } else if (tableName.equals("FILES")) {
                statement.executeUpdate("CREATE TABLE " + tableName + " (ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                        + " FILENAME VARCHAR(32)," + " EXTENSION VARCHAR(32), SIZE INTEGER, OWNER VARCHAR(32), FOREIGN KEY(OWNER) REFERENCES ONLINE_CLIENTS(USERNAME))");
            }
        }
    }

    //Here it prepares the form of the SQL queries to be executed: 
    public void prepareStatements(Connection connection) throws SQLException {
        //Statements that query on the ONLINE_CLIENTS table:
        addOnlineClient = connection.prepareStatement("INSERT INTO ONLINE_CLIENTS VALUES (?, ?, ?)");
        removeOnlineClient = connection.prepareStatement("DELETE FROM ONLINE_CLIENTS WHERE USERNAME = ? AND IP = ? AND PORT = ?");
        getClientByName = connection.prepareStatement("SELECT * FROM ONLINE_CLIENTS WHERE USERNAME = ?");
        //Starements that query on the FILES table:
        getAllFiles = connection.prepareStatement("SELECT * FROM FILES");
        getFilesByName = connection.prepareStatement("SELECT * FROM FILES WHERE FILENAME = ?");
        getFilesByExtention = connection.prepareStatement("SELECT * FROM FILES WHERE EXTENSION = ?");
        getFilesByOwner = connection.prepareStatement("SELECT * FROM FILES WHERE OWNER = ?");
        getFilesByMinimunSize = connection.prepareStatement("SELECT * FROM FILES WHERE SIZE<?");
        getFilesByMinimunSize = connection.prepareStatement("SELECT * FROM FILES WHERE SIZE>?");
        addFile = connection.prepareStatement("INSERT INTO FILES (FILENAME, EXTENSION, SIZE, OWNER) VALUES (?, ?, ?, ?)");
        removeFile = connection.prepareStatement("DELETE FROM FILES WHERE FILENAME = ? AND EXTENSION = ? AND OWNER = ?");
        removeAllFilesByClient = connection.prepareStatement("DELETE FROM FILES WHERE OWNER = ?");
        getFileByID = connection.prepareStatement("SELECT * FROM FILES WHERE ID = ?");
    }
}