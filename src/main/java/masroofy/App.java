package masroofy;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import masroofy.controller.AuthController;
import masroofy.controller.ThemeController;
import masroofy.session.UserSession;
import masroofy.view.AuthScene;
import masroofy.view.UiTheme;

/**
 * JavaFX application entry point for Masroofy.
 *
 * <p>
 * The app boots into either {@link InitScene} or {@link DashboardScene}
 * depending on whether an
 * active cycle exists in the local SQLite database.
 * </p>
 */
public class App extends Application {

    private static StackPane sceneRoot;
    private static Stage primaryStage;

    /**
     * Initializes the primary stage and shows the initial view.
     *
     * @param stage primary stage provided by JavaFX
     */
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        sceneRoot = new StackPane();

        new ThemeController().applySavedTheme();
        sceneRoot.setStyle("-fx-background-color: " + UiTheme.BG + ";");

        Scene scene = new Scene(sceneRoot, 900, 680);
        stage.setScene(scene);
        stage.setTitle("Masroofy — Budget Tracker");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();

        // Always require login on app open.
        new AuthController().signOut();
        UserSession.clear();
        new AuthScene(stage).show();
    }

    /**
     * Replaces the root content of the application's single {@link Scene}.
     *
     * @param content the node to display
     */
    public static void setContent(javafx.scene.Node content) {
        if (sceneRoot != null) {
            sceneRoot.setStyle("-fx-background-color: " + UiTheme.BG + ";");
        }
        sceneRoot.getChildren().setAll(content);
    }

    /**
     * Returns the primary JavaFX stage.
     *
     * @return primary stage
     */
    public static Stage getStage() {
        return primaryStage;
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args command line args
     */
    public static void main(String[] args) {
        launch(args);
    }
}
