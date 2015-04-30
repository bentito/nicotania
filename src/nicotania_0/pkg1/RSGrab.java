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
import javafx.scene.control.Label;

/**
 *
 * @author bentito
 */
public class RSGrab {

    String tableName = DBSetup.tableName;
    public String limit = "4000";
    public String selFields = "FileTag, Day, Hour, Minute, Second, NicLevel, Temp, Humidity"
            + " Humidity";
    public String day = "20";
    public String hour = "12";

    Connection conn = null;
    String SQL = null;
    
    public RSGrab(String dbToUseStr) {
        try {
            Class.forName("org.sqlite.JDBC");
            DBSetup db = new DBSetup();
            db.dbToUse = new File(dbToUseStr);
            conn = db.makeConn();
        } catch (ClassNotFoundException ex) {
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
        }
        return rs;
    }

    public void buildSQLStatement() {
//        SQL = "select * from ShimmerData WHERE Day=20 AND Hour=11 AND Minute=46;"; //debug testing with this small amount
        SQL = "SELECT";
        SQL += " ";
        SQL += selFields;
        SQL += " ";
        SQL += "FROM";
        SQL += " ";
        SQL += tableName;
        SQL += " ";
        SQL += "WHERE";
        SQL += " ";
        SQL += "Day = ";
        SQL += day;
        SQL += " AND ";
        SQL += "Hour = ";
        SQL += hour;
        SQL += ";";
    }

}
