import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // ✅ Signin en premier
        FXMLLoader loader =
                new FXMLLoader(getClass().getResource("/SigninView.fxml"));

        Scene scene = new Scene(loader.load(), 1440, 1024);

        scene.getStylesheets().add(
                getClass().getResource("/styleUser.css").toExternalForm()
        );

        stage.setTitle("VOS - Connexion");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
