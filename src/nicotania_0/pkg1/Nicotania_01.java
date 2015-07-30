/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicotania_0.pkg1;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author bentito
 */
public class Nicotania_01 extends Application {

    Connection conn = null;

    @Override
    public void start(Stage stage) throws Exception {
        File fileTemp = new File("link.ser");
        if (fileTemp.exists()) {
            fileTemp.delete();
        }
        fileTemp = new File("dbLoaded.txt");
        if (fileTemp.exists()) {
            fileTemp.delete();
        }
        Parent root = FXMLLoader.load(getClass().getResource("MainDocument.fxml"));
//        Parent loaded = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainDocument.fxml"));
        try {
            root = (Parent) loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(root);

        MenuBar menuBar = new MenuBar();

        // --- Menu File
        Menu menuFile = new Menu("File");

        MenuItem optItem = new MenuItem("Options...");
        MenuItem quitItem = new MenuItem("Quit");

        optItem.setOnAction((ActionEvent e) -> {
            try {
                Stage dialog = new Stage();
                dialog.initStyle(StageStyle.UTILITY);
                FXMLLoader optLoader = new FXMLLoader(getClass().getResource("FXMLOptionDialog.fxml"));
                Parent optDialog = (Parent) optLoader.load();
                Scene optScene = new Scene(optDialog);
                dialog.setScene(optScene);
                dialog.show();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        quitItem.setOnAction((ActionEvent e) -> {
            Platform.exit();
        });

        menuFile.getItems().add(optItem);
        menuFile.getItems().add(quitItem);

        menuBar.getMenus().addAll(menuFile);

        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac")) {
            menuBar.useSystemMenuBarProperty().set(true);
        }

        ((AnchorPane) scene.getRoot()).getChildren().addAll(menuBar);

        stage.setScene(scene);
        scene.getStylesheets().add(Nicotania_01.class
                .getResource("caspian.css").toExternalForm());
        stage.show();

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
//                Connection conn = controller.getConn();
//                System.out.println("debug: running and have conn to use");

            }
        });

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
        launch(args);
    }

}
