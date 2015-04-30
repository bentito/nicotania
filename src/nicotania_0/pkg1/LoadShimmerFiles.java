/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicotania_0.pkg1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author bentito
 */
public class LoadShimmerFiles {

    String rootDir = null;
    List<String> files = new ArrayList<>();
    List<Path> paths = new ArrayList<>();

    public LoadShimmerFiles(String rootDir) {
        this.rootDir = rootDir;

        files = getFileNames(files, Paths.get(rootDir));
    }

    public List<String> getFiles() {
        return files;
    }

    private List<String> getFileNames(List<String> fileNames, Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if (path.toFile().isDirectory()) {
                    getFileNames(fileNames, path);
                } else {
                    fileNames.add(path.toRealPath().toString());
                    paths.add(path);
//                    System.out.println(path.getFileName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileNames;
    }

    void loadAllFileData() throws SQLException, ClassNotFoundException {
        ArrayList items = new ArrayList<>();

        DBSetup dbd = new DBSetup();
        
        Connection conn = dbd.makeConn();
        dbd.dbToUse = new File(dbd.defaultDB);
        dbd.createTable();
        conn.setAutoCommit(false);

        Statement stmt = conn.createStatement();

        for (Path path : paths) {
            String fileTag = getFileTag(path);
            try (BufferedReader reader = Files.newBufferedReader(path, Charset.defaultCharset())) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    items = lineSplitter(line);
                    insertItems(stmt, items, fileTag);
//                    conn.commit();
                }
            } catch (IOException x) {
                System.err.format("IOException: %s%n", x);
            }
        }
        conn.commit(); // this IS a lot faster
        conn.close();
    }

    private void insertItems(Statement stmt, ArrayList items, String fileTag) {
        try {
            String valueString = "NULL,";
            String itemString = "";

            items.add(0, fileTag);
            for (Object item : items) {
                if (item instanceof Integer) {
                    itemString = Integer.toString((Integer) item);
                } else {
                    itemString = "'" + item + "'";
                }
                valueString += itemString;
                valueString += ",";
            }

            valueString = valueString.substring(0, valueString.length() - 1);
//            System.out.println("debug: " + valueString);
            String sql = "INSERT INTO " + DBSetup.tableName + " (ID,Filetag,Day,Hour,Minute,Second,NicLevel,Temp,Humidity) "
                    + "VALUES (" + valueString + ");";
            stmt.executeUpdate(sql);
            stmt.close();

        } catch (SQLException se) {
            System.err.println(se.getClass().getName() + ": " + se.getMessage());
            System.exit(0);
        }
    }

    ArrayList lineSplitter(String line) {
        // this is a line: 30:23:59:59:DHMS:NTH:01f7,089b,05b2
        ArrayList items = new ArrayList<>();
        int fieldCnt = 0;
        String values = null;
        for (String item : line.split(":")) {
            switch (fieldCnt) {
                case 0:
                    if (!hasHex(item)) {
                        Integer day = Integer.valueOf(item);
                        items.add(day);
                    } else {
                        items.add(31+Integer.parseInt(item, 16)); //gotta put something here, so day "31+dec(hex)"
                    }
                    break;
                case 1:
                    Integer hour = Integer.valueOf(item);
                    items.add(hour);
                    break;
                case 2:
                    Integer min = Integer.valueOf(item);
                    items.add(min);
                    break;
                case 3:
                    Integer sec = Integer.valueOf(item);
                    items.add(sec);
                    break;
                case 4:
//                    String dayFields = item;
//                    items.add(dayFields);
                    break;
                case 5:
//                    String valueFields = item;
//                    items.add(valueFields);
                    break;
                case 6:
                    values = item;
                    break;
                default:
                    break;
            }
            fieldCnt++;
        }

        fieldCnt = 0;
        for (String valItem : values.split(",")) {
            switch (fieldCnt) {
                case 0:
                    Integer nicLevel = Integer.parseInt(valItem, 16);
                    items.add(nicLevel);
                    break;
                case 1:
                    Integer temp = Integer.parseInt(valItem, 16);
                    items.add(temp);
                    break;
                case 2:
                    Integer humidity = Integer.parseInt(valItem, 16);
                    items.add(humidity);
                    break;
                default:
                    break;
            }
            fieldCnt++;
        }
        return items;
    }

    private String getFileTag(Path path) {
        String fileTag = path.toString();
        int idxStringToSearchOn = fileTag.lastIndexOf("/", fileTag.length());
        idxStringToSearchOn = fileTag.lastIndexOf("/", idxStringToSearchOn-1);
        fileTag = fileTag.substring(idxStringToSearchOn, fileTag.length());
        fileTag += "/";
        return fileTag;
    }

    public static boolean hasHex(String input) {
        boolean hasHex = false;
        try {
            Integer.valueOf(input);
        } catch (NumberFormatException e) {
            hasHex = true;
        }
        return hasHex;
    }

}
