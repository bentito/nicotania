/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicotania_0.pkg1;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextArea;

/**
 *
 * @author bentito
 */
public class RSOrderedGrab {

    String tableName = DBSetup.tableName;
    public String limit = "4000";
    public String selFields = "FileTag, Day, Hour, Minute, Second, NicLevel, Temp, Humidity"
            + " Humidity";
    public String day = "20";
    public String hour = "12";

    TextArea loggingTextArea = null;
    Connection conn = null;
    String SQL = null;
    
    public RSOrderedGrab(TextArea loggingTextArea, String dbToUse) {
        this.loggingTextArea = loggingTextArea;
        try {
            Class.forName("org.sqlite.JDBC");
            DBSetup db = new DBSetup();
            db.dbToUse = new File(dbToUse);
            conn = db.makeConn();
        } catch (ClassNotFoundException ex) {
            loggingTextArea.appendText("Problem loading JDBC:"+ex.getMessage());
            Logger.getLogger(RSGrab.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ResultSet executeQuery() {
        buildSQLStatement();
        ResultSet rs = null;
        try {
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(SQL);

        } catch (SQLException ex) {
            Logger.getLogger(RSGrab.class.getName()).log(Level.SEVERE, null, ex);
            loggingTextArea.appendText("Problem executing query:"+ex.getMessage());
        }
        return rs;
    }

    public void buildSQLStatement() {
//        SQL = "SELECT FileTag,Day,Hour,Minute,Second,NicLevel FROM ShimmerData ORDER BY Day,Hour,Minute,Second LIMIT 3000000;"; //debug testing with this small amount
        SQL = "SELECT FileTag,Day,Hour,Minute,Second,NicLevel FROM ShimmerData ORDER BY Day,Hour,Minute,Second;"; //debug
    }

}
