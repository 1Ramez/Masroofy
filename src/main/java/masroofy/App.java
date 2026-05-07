package masroofy;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import masroofy.controller.CycleController;
import masroofy.model.BudgetCycle;
import masroofy.view.DashboardScene;
import masroofy.view.InitScene;

public class App extends Application {

    private static StackPane sceneRoot;
    private static Stage primaryStage;

    @Override
    public void start(Stage stage){
        primaryStage = stage;
        sceneRoot = new StackPane();
        sceneRoot.setStyle("-fx-background-color: #0D0D0D;");

        Scene scene = new Scene(sceneRoot, 900, 680);
        stage.setScene(scene);
        stage.setTitle("Masroofy — Budget Tracker");
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();

        CycleController cc = new CycleController();
        BudgetCycle active = cc.checkActiveCycle();

        if (active == null){
            new InitScene(stage).show();
        }else{
            new DashboardScene(stage, active).show();
        }
    }

    public static void setContent(javafx.scene.Node content){
        sceneRoot.getChildren().setAll(content);
    }

    public static Stage getStage(){
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
