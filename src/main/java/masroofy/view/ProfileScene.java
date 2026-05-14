package masroofy.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import masroofy.App;
import masroofy.model.BudgetCycle;
import masroofy.session.UserSession;

/**
 * Simple profile screen showing the current account.
 */
public class ProfileScene {

    private final Stage stage;
    private final BudgetCycle cycle;

    public ProfileScene(Stage stage, BudgetCycle cycle) {
        this.stage = stage;
        this.cycle = cycle;
    }

    public void show() {
        BorderPane shell = new BorderPane();
        shell.setStyle("-fx-background-color: " + UiTheme.BG + ";");
        shell.setLeft(Sidebar.build(stage, cycle, "profile"));

        VBox content = new VBox(14);
        content.setPadding(new Insets(32));
        content.setAlignment(Pos.TOP_LEFT);
        content.setStyle("-fx-background-color: " + UiTheme.BG + ";");

        Label title = new Label("Profile");
        title.setFont(Font.font("Segoe UI", 24));
        title.setTextFill(Color.web(UiTheme.TEXT));

        String fullName = UserSession.getCurrentUser() == null ? "User" : UserSession.getCurrentUser().getName();
        Label name = new Label(fullName == null || fullName.isBlank() ? "User" : fullName.trim());
        name.setFont(Font.font("Segoe UI", 16));
        name.setTextFill(Color.web(UiTheme.TEXT_MUTED));

        VBox card = new VBox(10);
        card.setPadding(new Insets(18));
        card.setMaxWidth(520);
        card.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-border-color: %s;
                -fx-background-radius: 12;
                -fx-border-radius: 12;
                -fx-border-width: 1;
                """, UiTheme.SURFACE, UiTheme.BORDER));

        Label hint = new Label("Your progress is saved separately per account.");
        hint.setFont(Font.font("Segoe UI", 13));
        hint.setTextFill(Color.web(UiTheme.TEXT_DIM));

        card.getChildren().addAll(
                row("Name", fullName),
                hint);

        content.getChildren().addAll(title, name, card);
        shell.setCenter(content);

        App.setContent(shell);
    }

    private VBox row(String label, String value) {
        VBox box = new VBox(2);
        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", 12));
        l.setTextFill(Color.web(UiTheme.TEXT_DIM));
        Label v = new Label(value == null ? "" : value.trim());
        v.setFont(Font.font("Segoe UI", 14));
        v.setTextFill(Color.web(UiTheme.TEXT));
        box.getChildren().addAll(l, v);
        return box;
    }
}

