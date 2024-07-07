package view_controller;

import java.util.Optional;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class Main extends Application {
	private MainPane mainPane;
    @Override
    public void start(Stage primaryStage) throws Exception {
        mainPane = new MainPane();
        Scene scene = new Scene(mainPane, 800, 600);
        
        primaryStage.setTitle("JavaFX Paint Tool");
        primaryStage.setScene(scene);
        primaryStage.show();
        openAlert();
        primaryStage.setOnCloseRequest(event -> {
			saveWindow();
		});
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    private void openAlert() {
    	Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setHeaderText("Click cancel to start fresh.");
		alert.setContentText("Click OK to start from where you left off");
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			mainPane.loadDrawing();
		} else {
			
		}
    }
    
    private void saveWindow() {
	    Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setHeaderText("Click cancel to to not save any changes");
		alert.setContentText("To save the current state, click OK");
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			mainPane.saveDrawing();
		} else {
		}
    }
}