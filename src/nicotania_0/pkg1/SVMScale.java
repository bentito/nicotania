/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicotania_0.pkg1;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to scale values for use in SVM-based prediction y-max would be
 * the highest value for a given attribute (like raw resistance value of
 * nicotine, temp, etc.); y-min would be the highest value for a given attribute
 * (like raw resistance value of nicotine, temp, etc.); y-max and y-min would be
 * per database (i.e. per data sucked in from one sensor unit one-time); y-upper
 * is 1 y-lower is 0 y-upper and y-lower are choices that allow the data to work
 * well in libSVM library value is the y value, so in this case either raw
 * resistance value for nicotine, temp or humidity
 *
 * scaled values are stored back to the database for later use
 *
 * here's the scaling formula from svm_scale.java that came with libSVM
 *
 * if(value == y_min) value = y_lower; else if(value == y_max) value = y_upper;
 * else value = y_lower + (y_upper-y_lower) * (value-y_min) / (y_max-y_min);
 *
 * Added a table to the database called SVM, structure is: CREATE TABLE "SVM"
 * ("ID" INTEGER PRIMARY KEY NOT NULL , "SVM_NicLevel" FLOAT, "SVM_Temp" FLOAT,
 * "SVM_Humidity" FLOAT)
 *
 * Need to populate this table with the SVM scaled values, can do that in one
 * pass at analysis time.
 *
 * Then searches can take form of:
 *
 * SELECT * FROM ShimmerData, SVM WHERE ShimmerData.ID=SVM.ID AND Hour=7;
 *
 * So searches will return needed SVM-scaled data values, like:
 *
 * "1","/S204 sensor trial
 * 11.4.13/000/","21","7","37","47","1808","2070","1578","1","0.9","0.8","0.7"
 * ----------------------------------------------------------------------------------^^^---^^^---^^^
 * being the SVM-scaled float values
 *
 * @author bentito
 */
public class SVMScale {

    double yUpper = 1.0;
    double yLower = 0.0;

    public SVMScale(DBSetup db) {
        // test existence of SVM table, if not present, create and populate it
        Connection connection = db.makeConn();
        DatabaseMetaData md;
        boolean svmPresent = false;
        try {
            connection.setAutoCommit(false);
            md = connection.getMetaData();

            ResultSet rs = md.getTables(null, null, "%", null);

            while (rs.next()) {
//                System.out.println(rs.getString(3));
                if (rs.getString(3).equals("SVM")) {
                    svmPresent = true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(SVMScale.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (!svmPresent) {
            Statement stmt = null;
            Statement insStmt = null;
            try {
                stmt = connection.createStatement();
                insStmt = connection.createStatement();
            } catch (SQLException ex) {
                Logger.getLogger(SVMScale.class.getName()).log(Level.SEVERE, null, ex);
            }
            String tableName = "SVM";
            String sql = null;
            sql = "CREATE TABLE IF NOT EXISTS " + tableName
                    + " (ID INTEGER PRIMARY KEY NOT NULL,"
                    + " SVM_NicLevel FLOAT,"
                    + " SVM_Temp FLOAT,"
                    + " SVM_Humidity FLOAT);";
            System.out.println("debug: sql stmt: " + sql);
            try {
                stmt.executeUpdate(sql);
                stmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(SVMScale.class.getName()).log(Level.SEVERE, null, ex);
            }
            boolean stillFindingRecords = true;
            int rsInc = 100000; // do a big chunk (rsInc) of SELECTs and corresponding INSERTs then commit at end of each chunk for speed
            int count = 0;
            int totRet = 0;
            String begin;
            String end;
            String ID;
            String valueString = "";
            int nicLevel;
            int temp;
            int humidity;
            double SVMnicLevel;
            double SVMtemp;
            double SVMhumidity;
            int maxNicLevel = 0;
            int minNicLevel = 0;
            int maxTemp = 0;
            int minTemp = 0;
            int maxHumidity = 0;
            int minHumidity = 0;
            ResultSet rs;

            //get yMax and yMin from database       
            try {
                sql = "SELECT max(NicLevel) from ShimmerData;";
                rs = stmt.executeQuery(sql);
                maxNicLevel = rs.getInt("max(NicLevel)");
                sql = "SELECT min(NicLevel) from ShimmerData;";
                rs = stmt.executeQuery(sql);
                minNicLevel = rs.getInt("min(NicLevel)");

                sql = "SELECT max(Temp) from ShimmerData;";
                rs = stmt.executeQuery(sql);
                maxTemp = rs.getInt("max(Temp)");
                sql = "SELECT min(Temp) from ShimmerData;";
                rs = stmt.executeQuery(sql);
                minTemp = rs.getInt("min(Temp)");

                sql = "SELECT max(Humidity) from ShimmerData;";
                rs = stmt.executeQuery(sql);
                maxHumidity = rs.getInt("max(Humidity)");
                sql = "SELECT min(Humidity) from ShimmerData;";
                rs = stmt.executeQuery(sql);
                minHumidity = rs.getInt("min(Humidity)");
            } catch (SQLException ex) {
                Logger.getLogger(SVMScale.class.getName()).log(Level.SEVERE, null, ex);
            }

            while (stillFindingRecords) {
                // run through all values in the ShimmerData table and create 
                // scaled values for them according to the formula 
                // if(value == y_min) value = y_lower; 
                // else if(value == y_max) value = y_upper;
                // else value = y_lower + (y_upper-y_lower) * (value-y_min) / (y_max-y_min);

                begin = Integer.toString(count);
                end = Integer.toString(count + rsInc);
                sql = "select * from ShimmerData WHERE ID>" + begin + " AND ID<=" + end + ";";
                try {
                    rs = stmt.executeQuery(sql);
                    while (rs.next()) {
                        totRet++;
                        ID = rs.getString("ID");
                        nicLevel = rs.getInt("NicLEvel");
                        temp = rs.getInt("Temp");
                        humidity = rs.getInt("Humidity");

                        if (nicLevel == minNicLevel) {
                            SVMnicLevel = yLower;
                        } else if (nicLevel == maxNicLevel) {
                            SVMnicLevel = yUpper;
                        } else {
                            SVMnicLevel = yLower + (yUpper - yLower) * (nicLevel - minNicLevel) / (maxNicLevel - minNicLevel);
                        }

                        if (temp == minTemp) {
                            SVMtemp = yLower;
                        } else if (temp == maxTemp) {
                            SVMtemp = yUpper;
                        } else {
                            SVMtemp = yLower + (yUpper - yLower) * (temp - minTemp) / (maxTemp - minTemp);
                        }

                        if (humidity == minHumidity) {
                            SVMhumidity = yLower;
                        } else if (humidity == maxHumidity) {
                            SVMhumidity = yUpper;
                        } else {
                            SVMhumidity = yLower + (yUpper - yLower) * (humidity - minHumidity) / (maxHumidity - minHumidity);
                        }

                        valueString += ID;
                        valueString += ",";
                        valueString += SVMnicLevel;
                        valueString += ",";
                        valueString += SVMtemp;
                        valueString += ",";
                        valueString += SVMhumidity;

                        sql = "INSERT INTO " + tableName + " (ID, SVM_NicLevel,SVM_Temp,SVM_Humidity) "
                                + "VALUES (" + valueString + ");";
                        insStmt.execute(sql);
                        valueString = "";
                    }
                    if (totRet < rsInc) {
                        stillFindingRecords = false;
                    }
                    totRet = 0;
                } catch (SQLException ex) {
                    Logger.getLogger(SVMScale.class.getName()).log(Level.SEVERE, null, ex);

                } finally {
                    try {
                        stmt.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(SVMScale.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                count = count + rsInc;
                try {
                    connection.commit();
                } catch (SQLException ex) {
                    Logger.getLogger(SVMScale.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.print("done finding records");
        }
        try {
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(SVMScale.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
