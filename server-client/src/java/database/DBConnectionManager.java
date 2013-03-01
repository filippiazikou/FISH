package database;

import java.util.*;
import java.sql.*;

//Connection manager is responsible for creating a pool of connections
//that can operate on a Database given.
public class DBConnectionManager {

    private Vector connectionPool = new Vector();
    private Database db;

    public DBConnectionManager(Database db) {
        this.db = db;
        initializeConnectionPool();
    }

    private void initializeConnectionPool() {
        while (!checkIfConnectionPoolIsFull()) {
            //Adding new connection instance until the pool is full
            connectionPool.addElement(createNewConnectionForPool());
        }
        System.out.println("Pool of DB Connections is full and ready.");
    }

    //Creating a connection
    private Connection createNewConnectionForPool() {
        Connection connection = null;
        try {
            connection = db.createNewConnection();
        } catch (SQLException sqle) {
            System.err.println("SQLException while creating new connection: " + sqle);
            return null;
        } catch (ClassNotFoundException cnfe) {
            System.err.println("ClassNotFoundException while creating new connection: " + cnfe);
            return null;
        }
        return connection;
    }

    public synchronized Connection getConnectionFromPool() {
        Connection connection = null;

        //Check if there is a connection available. There are times when all the connections in the pool may be used up
        if (connectionPool.size() > 0) {
            connection = (Connection) connectionPool.firstElement();
            connectionPool.removeElementAt(0);
        }
        //Giving away the connection from the connection pool
        return connection;
    }

    private synchronized boolean checkIfConnectionPoolIsFull() {
        //This is were we difine the size of the pool.
        //The number can change according to the needs of the application.
        final int MAX_POOL_SIZE = 5;
        //Check if the pool size
        if (connectionPool.size() < MAX_POOL_SIZE) {
            return false;
        }
        return true;
    }

    public synchronized void returnConnectionToPool(Connection connection) {
        //Adding the connection from the client back to the connection pool
        connectionPool.addElement(connection);
    }
}