import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ServerMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/main_panel.fxml"));
        primaryStage.setTitle("Server");
        primaryStage.setScene(new Scene(root, 400, 100));
        primaryStage.show();
    }


    public static void main(String[] args) throws IOException {
        launch(args);
    }
}