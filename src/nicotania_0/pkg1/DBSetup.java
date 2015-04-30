package nicotania_0.pkg1;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBSetup {

    public Connection conn = null;
    public static String tableName = "ShimmerData";
    public File dbToUse = null;
    public String dbURL = null;
    public String defaultDB = "nicotania.db";

    //Intended usage:
    // Instantiate class; 
    // Assign value for dbToUse to be the sqlite database to use
    //  if no dBToUse assigned, will default to using nicotania.db for database name
    // call makeConn, you get a connection back for SELECTS;
    // call createTable for table creation you get a connection back for INSERTS;
    //
    DBSetup() {

    }

    public Connection makeConn() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DBSetup.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (dbToUse != null) {
            dbURL = "jdbc:sqlite:" + dbToUse.getAbsolutePath();
        } else {
            dbURL = "jdbc:sqlite:"+defaultDB;
        }
        
        try {
            conn = DriverManager.getConnection(dbURL);
        } catch (SQLException ex) {
            Logger.getLogger(DBSetup.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conn;
    }

    public Connection createTable() {

        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            dbURL = "jdbc:sqlite:" + dbToUse.getAbsolutePath();
            conn = DriverManager.getConnection(dbURL);
            System.out.println("Opened database successfully");
            stmt = conn.createStatement();
            String sql = null;
//            sql = "DROP TABLE IF EXISTS " + tableName + ";";
//            stmt.executeUpdate(sql);
            sql = "CREATE TABLE IF NOT EXISTS " + tableName
                    + " (ID INTEGER PRIMARY KEY AUTOINCREMENT," //0
                    + " FileTag TEXT NOT NULL,"//                 1  
                    + " Day INTEGER NOT NULL, "//                 2
                    + " Hour    INTEGER NOT NULL, "//             3
                    + " Minute  INTEGER NOT NULL, "//             4
                    + " Second  INTEGER NOT NULL, "//             5
                    + " NicLevel    INTEGER    NOT NULL, "//      6
                    + " Temp    INTEGER    NOT NULL, "//          7
                    + " Humidity    INTEGER    NOT NULL);";//     8
//            System.out.println("debug: sql stmt: "+sql);
            stmt.executeUpdate(sql);
            stmt.close();
//            conn.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Table created successfully");
        return conn;
    }

    public static void main(String args[]) {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:nicotania.db");
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }
}
