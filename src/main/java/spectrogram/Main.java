package spectrogram;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import spectrogram_models.Global;

public class Main extends Application {

    public Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));

        Parent root = mainLoader.load();
        primaryStage.setTitle("The world is my instrument, this is my playlist.");
        primaryStage.setScene(new Scene(root, 800, 600));


        /* Global Keystrokes */
        primaryStage.getScene().getAccelerators().put( /* Register Ctrl + O keystroke reaction */
        new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN),
        () -> ((Controller)mainLoader.getController()).openExistingPlayList());
        stage = primaryStage;

        /* Set up Global */
        Global.setStage(this);


        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
