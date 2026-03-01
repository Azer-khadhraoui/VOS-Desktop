import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        System.out.println("1 - start lancé");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/SigninView.fxml"));

        System.out.println("2 - FXML trouvé");

        Scene scene = new Scene(loader.load(), 1440, 1024);

        System.out.println("3 - FXML chargé");

        scene.getStylesheets().add(
                getClass().getResource("/styleUser.css").toExternalForm());

        System.out.println("4 - CSS chargé");

        stage.setTitle("VOS - Connexion");
        stage.setResizable(true);
        stage.setMaximized(false);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

        System.out.println("5 - stage affiché");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
