/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicotania_0.pkg1;

import extfx.scene.chart.DateAxis;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * FXML Controller class
 *
 * @author bentito
 */
public class GraphViewController implements Initializable {

    Stage stage = null;
    private String dbLoaded = "nicotania.db";
    String day = "20";
    String hour = "12";
    String min = "0";
    String sec = "0";

    @FXML
    private AnchorPane graphMainAP;
    @FXML
    private ScrollPane graphSP;
    @FXML
    private ScrollPane tempGraphSP;
    @FXML
    private TextField dayField;
    @FXML
    private TextField hourField;
    @FXML
    private Label fileTagsLabel;
    @FXML
    private Label resultSetSize;

    @FXML
    private void handleBack1HourAction(ActionEvent event) {
        int currHourI = Integer.parseInt(hourField.getText());
        currHourI--;
        if (currHourI == -1) {
            hourField.setText("23");
            handleBack1DayAction(event);
        } else {
            String currHour = String.valueOf(currHourI);
            String currDay = dayField.getText();
            drawGraph(currDay, currHour);
        }
    }

    @FXML
    private void handleFwd1HourAction(ActionEvent event) {
        int currHourI = Integer.parseInt(hourField.getText());
        currHourI++;
        if (currHourI == 24) {
            hourField.setText("0");
            handleFwd1DayAction(event);
        } else {
            String currHour = String.valueOf(currHourI);
            String currDay = dayField.getText();
            drawGraph(currDay, currHour);
        }
    }

    @FXML
    private void handleBack1DayAction(ActionEvent event) {
        int currDayI = Integer.parseInt(dayField.getText());
        currDayI--;
        String currDay = String.valueOf(currDayI);
        String currHour = hourField.getText();
        drawGraph(currDay, currHour);
    }

    @FXML
    private void handleFwd1DayAction(ActionEvent event) {
        int currDayI = Integer.parseInt(dayField.getText());
        currDayI++;
        String currDay = String.valueOf(currDayI);
        String currHour = hourField.getText();
        drawGraph(currDay, currHour);
    }

    @FXML
    private void handleRefreshAction(ActionEvent event) {
        String currDay = dayField.getText();
        String currHour = hourField.getText();
        drawGraph(currDay, currHour);
    }

//    @FXML
//    private void handleNicGraphScroll(ScrollEvent event) {
//        double deltaNicX = event.getTotalDeltaX();
//        double currTempX = tempGraphSP.getHvalue();
//        tempGraphSP.setHvalue(currTempX + deltaNicX);
//    }
    private void drawGraph(String day, String hour) {
        RSGrab rsGrab = new RSGrab(dbLoaded);
        rsGrab.day = day;
        rsGrab.hour = hour;
        dayField.setText(day);
        hourField.setText(hour);

        ResultSet rs = rsGrab.executeQuery();

        XYChart.Series nicotineSeries = new XYChart.Series();
        XYChart.Series temperatureSeries = new XYChart.Series();
        nicotineSeries.setName("Nicotine");
        temperatureSeries.setName("Temperature");
        int timeInSec;
        Set<String> fileTags = new HashSet<>();

        double lowestNicReading = 10000000, lowestTempReading = 10000000;
        double highestNicReading = 0, highestTempReading = 0;
        double nicReading, tempReading;
        int rowCount = 0;
        try {
            while (rs.next()) {
                Calendar cal = Calendar.getInstance();
                
                cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(rs.getString("Hour")));
                cal.set(Calendar.MINUTE, Integer.parseInt(rs.getString("Minute")));
                cal.set(Calendar.SECOND, Integer.parseInt(rs.getString("Second")));
                Date hourMinSec = cal.getTime();

                nicotineSeries.getData().add(new XYChart.Data(hourMinSec,
                        Integer.parseInt(rs.getString("NicLevel"))));
                temperatureSeries.getData().add(new XYChart.Data(hourMinSec,
                        Integer.parseInt(rs.getString("Temp"))));
                fileTags.add(rs.getString("FileTag"));

                nicReading = Integer.parseInt(rs.getString("NicLevel"));
                tempReading = Integer.parseInt(rs.getString("Temp"));

                if (nicReading < lowestNicReading) {
                    lowestNicReading = nicReading;
                }

                if (tempReading < lowestTempReading) {
                    lowestTempReading = tempReading;
                }

                if (nicReading > highestNicReading) {
                    highestNicReading = nicReading;
                }

                if (tempReading > highestTempReading) {
                    highestTempReading = tempReading;
                }
                rowCount++;
            }
            resultSetSize.setText("datapoints drawn: " + String.valueOf(rowCount));
            rs.close();

        } catch (SQLException ex) {
            Logger.getLogger(GraphViewController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        fileTagsLabel.setText(fileTags.toString());

        final DateAxis xAxis = new DateAxis();
        final NumberAxis yAxis = new NumberAxis(lowestNicReading - 20, highestNicReading + 20, 5.0); // 5 is tick distance
        final NumberAxis yAxisTemp = new NumberAxis(lowestTempReading - 20, highestTempReading + 20, 5.0); // 5 is tick distance

        final LineChart<Date, Number> lc = new LineChart<>(xAxis, yAxis);
        final LineChart<Date, Number> lcTemp = new LineChart<>(xAxis, yAxisTemp);

        lc.setTitle("Nictotine vs. Seconds of the Hour");

        lc.getData().add(nicotineSeries);
        lcTemp.getData().add(temperatureSeries);
        lc.setPrefWidth(10000);
        lcTemp.setPrefWidth(10000);
        lc.setCreateSymbols(false);
        lcTemp.setCreateSymbols(false);
        lc.setHorizontalGridLinesVisible(false);
        lcTemp.setHorizontalGridLinesVisible(false);
        lc.setVerticalGridLinesVisible(false);
        lcTemp.setVerticalGridLinesVisible(false);

        lc.setPadding(new Insets(5, 5, 5, 13));//5 is default, 13 is to move the y axis over to the left, to align the graphs horizontally

        lcTemp.setStyle("CHART_COLOR_1: #0099FF;");

        graphSP.setContent(lc);
        tempGraphSP.setContent(lcTemp);
    }

    void deserializeLink() {
        try (
                InputStream fileIS = new FileInputStream("link.ser");
                InputStream buffer = new BufferedInputStream(fileIS);
                ObjectInput input = new ObjectInputStream(buffer);) {
            //deserialize the link
            ArrayList<String> timeDate = (ArrayList<String>) input.readObject();
            //get its data
            day = (String) timeDate.get(0);
            hour = (String) timeDate.get(1);
            min = (String) timeDate.get(2);
            sec = (String) timeDate.get(3);
            //refresh the graph
            drawGraph(day, hour);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MainDocumentController.class.getName()).log(Level.SEVERE, "Cannot deserialize link. Class not found.", ex);
        } catch (IOException ex) {
            Logger.getLogger(MainDocumentController.class.getName()).log(Level.SEVERE, "Cannot do file IO for deserialize.", ex);
        }
    }

    void autoMoveScroller() {
        // has to happen after stage.show()
        int min = 0;
        int sec = 0;
        min = Integer.parseInt(this.min);
        sec = Integer.parseInt(this.sec);
        graphSP.setHvalue((min * 60 + sec) / 3600.0);
//        System.out.println("debug: HValue: "+(min*60+sec)/3600.0);
        tempGraphSP.setHvalue((min * 60 + sec) / 3600.0);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        try {
            List<String> lines;
            lines = Files.readAllLines(Paths.get("dbLoaded.txt"), Charset.forName("UTF-8"));
            for (String line : lines) {
                dbLoaded = line;
            }
        } catch (IOException ex) {
            //just go with default db file
        }

        stage = new Stage();
        stage.setTitle("Graph View");

        stage.setScene(new Scene(graphMainAP));

        File fileTemp = new File("link.ser");
        if (fileTemp.exists()) {
            deserializeLink();
        } else {
            drawGraph(day, hour);
        }
        stage.setTitle(day + ":" + hour + ":" + min + ":" + sec);
        stage.show();

        autoMoveScroller();
        graphSP.hvalueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                    Number old_val, Number new_val) {
                tempGraphSP.setHvalue(new_val.doubleValue());
            }
        });

//        new Thread(new Runnable() {
//            public void run() {
//                // monitor link serialization file
//                TimerTask task = new FileWatcher(new File("link.ser")) {
//                    protected void onChange(File file) {
//                        // here we code the action on a change
//                        //deserialize the link.ser file
//                        Platform.runLater(new Runnable() {
//                            public void run() {
//                                // we are now back in the EventThread and can update the GUI
//                                deserializeLink();
//                            }
//                        });
//                    }
//                };
//
//                Timer timer = new Timer();
//                // repeat the check every N milliseconds
//                timer.schedule(task, new Date(), 300);
//            }
//        }).start();
    }
}
