/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicotania_0.pkg1;

import com.google.common.collect.ImmutableListMultimap;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 *
 * @author bentito
 */
public class MainDocumentController implements Initializable {

    private Preferences prefs = Preferences.userRoot().node("/Nicotania");
    public File selectedDirectory = null;
    boolean graphViewOpen = false;

    @FXML
    public Parent stageX;

    @FXML
    public Connection conn;

    @FXML
    public Label label;

    @FXML
    private Label dbLoadedLabel;

    @FXML
    private TextArea loggingTextArea;

    @FXML
    private TextFlow loggingTextFlow;

    DBSetup db = new DBSetup();

    @FXML
    private void handleAnalyzeButton(ActionEvent event) {
        if (db.dbToUse == null) {
            loggingTextArea.appendText("\nAnalysis for default database file: nicotania.db.\n");
        }

        if (db.dbToUse != null || checkDefaultDBPresent()) {
            loggingTextArea.appendText("\nAnalysis for default database file: " + db.dbToUse + "\n");
            RSOrderedGrab rsog = new RSOrderedGrab(loggingTextArea, dbLoadedLabel.getText());
            ResultSet rs = rsog.executeQuery();
            DescriptiveStatistics stats = new DescriptiveStatistics();
            DescriptiveStatistics rollingStats = new DescriptiveStatistics();
            int windowSize1 = prefs.getInt("Win1Size", 100);
            rollingStats.setWindowSize(windowSize1);

            ArrayList<String> highestNicLevel = null;
            ArrayList<String> lowestNicLevel = null;

            int nicLevel = 0;
            int highNicLevel = 0;
            ImmutableListMultimap.Builder<Double, ArrayList> highWin100Range = new ImmutableListMultimap.Builder<>();
            ImmutableListMultimap.Builder<Double, ArrayList> highWin100Kurtosis = new ImmutableListMultimap.Builder<>();
            highWin100Range.orderKeysBy(Collections.reverseOrder());
            highWin100Kurtosis.orderKeysBy(Collections.reverseOrder());

            int lowNicLevel = 10000000;

            double winRange = 0;
            double kurtosis = 0;

            try {

                while (rs.next()) {
                    nicLevel = Integer.parseInt(rs.getString("NicLevel"));

                    stats.addValue(nicLevel);
                    rollingStats.addValue(nicLevel);
                    ArrayList<String> timeDate = new ArrayList<>(); // put: day, hour, min, sec -- in that order
                    timeDate = putTimeDate(rs.getString("Day"), rs.getString("Hour"), rs.getString("Minute"), rs.getString("Second"));

                    if ((stats.getN() % windowSize1) == 0) {
                        winRange = rollingStats.getMax() - rollingStats.getMin();
                        kurtosis = rollingStats.getKurtosis();
                        highWin100Range.put(winRange, timeDate);
                        highWin100Kurtosis.put(kurtosis, timeDate);
                    }
                    if (nicLevel > highNicLevel) { //TODO: replace with apache math stats
                        highNicLevel = nicLevel;
                        highestNicLevel = timeDate;
                    }
                    if (nicLevel < lowNicLevel) { //TODO: replace with apache math stats
                        lowNicLevel = nicLevel;
                        lowestNicLevel = timeDate;
                    }
                }

                ImmutableListMultimap highWin100RangeILM = highWin100Range.build();
                ImmutableListMultimap highWin100KurtosisILM = highWin100Kurtosis.build();

                loggingTextArea.appendText("Done with analysis...\n");
                loggingTextArea.appendText(String.format("Number of Data Points in this Set: %,13d%n", stats.getN()));
                loggingTextArea.appendText(String.format("Mean Nicotine Level in this Set: %16.2f%n", stats.getMean()));
                loggingTextArea.appendText(String.format("Std Dev  of Nicotine Level: %25.2f%n", stats.getStandardDeviation()));

                printTF("Highest Nicotine Level Found in this Set: " + String.valueOf(highNicLevel));
                printTF(formatTimeDate(highestNicLevel), highestNicLevel);

                printTF("Lowest Nicotine Level Found in this Set: " + String.valueOf(lowNicLevel));
                printTF(formatTimeDate(lowestNicLevel), lowestNicLevel);

                printTF("Top Nic Level Ranges in 100 Sec. Window were:");
                Iterator itr = highWin100RangeILM.keySet().iterator();
                for (int idx = 0; idx < 10; idx++) {
                    if (itr.hasNext()) {
                        Collection<ArrayList> values = highWin100RangeILM.get(itr.next());
                        for (ArrayList value : values) {
                            printTF(formatTimeDate(value), value);
                        }
                    }
                }
                printTF("Top Nic Level Kurtosis values in 100 Sec. Windows were:");
                itr = highWin100KurtosisILM.keySet().iterator();
                for (int idx = 0; idx < 10; idx++) {
                    if (itr.hasNext()) {
                        Collection<ArrayList> values = highWin100KurtosisILM.get(itr.next());
                        for (ArrayList value : values) {
                            printTF(formatTimeDate(value), value);
                        }
                    }
                }

            } catch (SQLException ex) {
                Logger.getLogger(MainDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                loggingTextArea.appendText("SQL Exception: " + ex.getMessage());
            }
        }
    }

    @FXML
    private void handleButtonAction(ActionEvent event
    ) {
        Stage stage = (Stage) stageX.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory == null) {
            //do nothing
        } else {
            label.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void handleLoadDataAction(ActionEvent event) throws SQLException, ClassNotFoundException {
        String selectedDirectoryStr = label.getText();
//        System.out.println(selectedDirectoryStr); //debug
//        LoadShimmerFiles lsf = null;
//        ArrayList<String> files = null;
//
//        if (selectedDirectory == null) {
//            //do nothing
//        } else {
//            lsf = new LoadShimmerFiles(selectedDirectory.getAbsolutePath());
//            files = (ArrayList) lsf.getFiles();
//        }
//        if (lsf != null && files != null) {
        loggingTextArea.appendText("\n");
        loggingTextArea.appendText("Loading data into database. Will take a minute or two.\n");
        new Thread(new Runnable() {
            public void run() {
                LoadShimmerFiles lsf = null;
                ArrayList<String> files = null;
                if (selectedDirectory == null) {
                    //do nothing
                } else {
                    lsf = new LoadShimmerFiles(selectedDirectory.getAbsolutePath());
                    files = (ArrayList) lsf.getFiles();
                }
                try {
                    lsf.loadAllFileData();
                } catch (Exception e) {
                }

                // we are not in the event thread currently so we should not update the UI here
                // this is a good place to do some slow, background loading, e.g. load from a server or from a file system 
                Platform.runLater(new Runnable() {
                    public void run() {
                        // we are now back in the EventThread and can update the GUI
                        loggingTextArea.appendText("Completed database insertions.\n");
                    }
                });

            }
        }).start();
    }

    @FXML
    private void handleLoadDbAction(ActionEvent event
    ) {
        Stage stage = (Stage) stageX.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        FileChooser fileChooser = new FileChooser();
        File dbFile = fileChooser.showOpenDialog(stage);

        if (dbFile == null) {
            dbLoadedLabel.setText(new File(db.defaultDB).getAbsolutePath());
        } else {
            dbLoadedLabel.setText(dbFile.getAbsolutePath());
            db.dbToUse = dbFile;
        }
        if (db.dbToUse != null) {
            try {
                File fileTemp = new File("dbLoaded.txt");
                if (fileTemp.exists()) {
                    fileTemp.delete();
                }
                PrintWriter out = new PrintWriter("dbLoaded.txt"); //keeping it simple, need to get one thing to the other view/controller
                out.println(db.dbToUse);
                out.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @FXML
    private void handle10GraphAction(ActionEvent event) throws ClassNotFoundException, IOException {
        if (!graphViewOpen) {
            if (db.dbToUse != null || checkDefaultDBPresent()) {
                if (db.dbToUse == null) {
                    loggingTextArea.appendText("\nGraphing from default database file: nicotania.db.\n");
                }
                File fileTemp = new File("link.ser");
                if (fileTemp.exists()) {
                    fileTemp.delete();
                }
                FXMLLoader loader = new FXMLLoader(getClass().getResource("GraphView.fxml"));

                Parent root = (Parent) loader.load();
                graphViewOpen = true;
            }
        }
    }

    private boolean checkDefaultDBPresent() {
        boolean present = false;
        DBSetup db = new DBSetup();

        File fileTemp = new File(db.defaultDB);
        if (fileTemp.exists()) {
            return true;
        } else {
            loggingTextArea.appendText("\nDefault Database " + db.defaultDB + " doesn't exist\n");
            loggingTextArea.appendText(":::::::> Use Set Shimmer Dir and Load Shimmer Data to create a new one...\n");
        }
        return present;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        loggingTextFlow.getChildren().add(new Text("Analysis output...\n"));

    }

    private ArrayList<String> putTimeDate(String day, String hour, String min, String sec) {
        ArrayList<String> timeDate = new ArrayList();
        timeDate.add(day);
        timeDate.add(hour);
        timeDate.add(min);
        timeDate.add(sec);
        return timeDate;
    }

    private String formatTimeDate(ArrayList timeDate) {
//        get: day, hour, min, sec -- in that order
        String formattedStr = null;
        formattedStr = "On day ";
        formattedStr += (String) timeDate.get(0);
        formattedStr += " at ";
        formattedStr += padToTwo((String) timeDate.get(1));
        formattedStr += ":";
        formattedStr += padToTwo((String) timeDate.get(2));
        formattedStr += ":";
        formattedStr += padToTwo((String) timeDate.get(3));
        return formattedStr;
    }

    private String padToTwo(String needsPadding) {
        return String.format("%02d", Integer.parseInt(needsPadding));
    }

    private void printTF(String toPrint, ArrayList<String> linkTo) {
        Hyperlink link = new Hyperlink(toPrint);
        link.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                //serialize the link
                try (
                        OutputStream file = new FileOutputStream("link.ser");
                        OutputStream buffer = new BufferedOutputStream(file);
                        ObjectOutput output = new ObjectOutputStream(buffer);) {
                    output.writeObject(linkTo);
                } catch (IOException ex) {
                    loggingTextArea.appendText("Link Serialize Problem: " + ex.getMessage());
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource("GraphView.fxml"));

                try {
                    Parent root = (Parent) loader.load();
                } catch (IOException ex) {
                    Logger.getLogger(MainDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        loggingTextFlow.getChildren().add(link);
        loggingTextFlow.getChildren().add(new Text("\n"));
    }

    private void printTF(String toPrint) {
        loggingTextFlow.getChildren().add(new Text(toPrint + "\n"));
    }

}
