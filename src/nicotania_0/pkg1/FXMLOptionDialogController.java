/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nicotania_0.pkg1;

//import Toast;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
//import javax.swing.AbstractAction;
//import org.controlsfx.control.NotificationPane;

/**
 * FXML Controller class
 *
 * @author bentito
 */
public class FXMLOptionDialogController implements Initializable {

    private Preferences prefs;
//    NotificationPane notificationPane;

    @FXML
    private TextField win1TextField;

    @FXML
    private TabPane prefsTabPane;

    @FXML
    private Button saveButton;

    @FXML
    private void handleSaveButton(ActionEvent event) {

        Stage stage = (Stage) saveButton.getScene().getWindow();

        try {
            prefs.putInt("Win1Size", Integer.parseInt(win1TextField.getText()));
             stage.close();
        } catch (NumberFormatException nfe) {
            Toast toast = Toast.makeText("Window Sizes must be numbers of seconds.", Duration.seconds(2.0));
            toast.show(stage);
            toast.centerOnScreen();
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SingleSelectionModel<Tab> selectionModel = prefsTabPane.getSelectionModel();
        selectionModel.select(1); // choose the 2nd tab to be the one that shows first

        // This will define a node in which the preferences can be stored
        prefs = Preferences.userRoot().node("/Nicotania");
        win1TextField.setText(String.valueOf(prefs.getInt("Win1Size", 100)));
    }

}
