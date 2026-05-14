package masroofy.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import masroofy.controller.AuthController;
import masroofy.model.BudgetCycle;
import masroofy.session.UserSession;

/**
 * Shared left sidebar navigation used across the app screens.
 */
public final class Sidebar {

    private Sidebar() {
    }

    public static VBox build(Stage stage, BudgetCycle cycle, String activeKey) {
        VBox sidebar = new VBox(6);
        sidebar.setPrefWidth(210);
        sidebar.setPadding(new Insets(14, 10, 14, 10));
        sidebar.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-border-color: %s;
                -fx-border-width: 0 1 0 0;
                """, UiTheme.BG, UiTheme.BORDER));

        Label navTitle = new Label("Navigation");
        navTitle.setFont(Font.font("Segoe UI", 11));
        navTitle.setStyle("-fx-font-weight: bold;");
        navTitle.setTextFill(javafx.scene.paint.Color.web(UiTheme.TEXT_DIM));

        sidebar.getChildren().add(navTitle);

        sidebar.getChildren().addAll(
                navButton("Dashboard", "dashboard", activeKey, () -> {
                    if (cycle == null) {
                        new InitScene(stage).show();
                        return;
                    }
                    new DashboardScene(stage, cycle).show();
                }),
                navButton("Log Expense", "expense", activeKey, () -> {
                    if (cycle == null) {
                        new InitScene(stage).show();
                        return;
                    }
                    new ExpenseScene(stage, cycle).show();
                }),
                navButton("History", "history", activeKey, () -> {
                    if (cycle == null) {
                        new InitScene(stage).show();
                        return;
                    }
                    new HistoryScene(stage, cycle).show();
                }),
                navButton("Stats", "stats", activeKey, () -> {
                    if (cycle == null) {
                        new InitScene(stage).show();
                        return;
                    }
                    new StatsScene(stage, cycle).show();
                }));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        sidebar.getChildren().add(sectionDivider());

        String name = UserSession.getCurrentUser() == null ? "User" : UserSession.getCurrentUser().getName();
        String trimmed = name == null ? "" : name.trim();
        String firstName = trimmed.isBlank() ? "User" : trimmed.split("\\s+")[0];

        sidebar.getChildren().add(profileChip(firstName));

        sidebar.getChildren().addAll(
                navButton("Profile", "profile", activeKey, () -> new ProfileScene(stage, cycle).show()),
                navButton("Settings", "settings", activeKey, () -> {
                    if (cycle == null) {
                        new InitScene(stage).show();
                        return;
                    }
                    new SettingsScene(stage, cycle).show();
                }),
                dangerButton("Logout", () -> {
                    new AuthController().signOut();
                    DashboardScene.resetSession();
                    new AuthScene(stage).show();
                }));

        return sidebar;
    }

    private static Node sectionDivider() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setStyle("-fx-background-color: " + UiTheme.BORDER + ";");
        VBox.setMargin(r, new Insets(10, 0, 10, 0));
        return r;
    }

    private static Node profileChip(String firstName) {
        VBox chip = new VBox(2);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(10));
        chip.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-border-color: %s;
                -fx-background-radius: 10;
                -fx-border-radius: 10;
                -fx-border-width: 1;
                """, UiTheme.SURFACE, UiTheme.BORDER));

        Label hi = new Label("Signed in as");
        hi.setFont(Font.font("Segoe UI", 11));
        hi.setTextFill(javafx.scene.paint.Color.web(UiTheme.TEXT_DIM));

        Label name = new Label(firstName);
        name.setFont(Font.font("Segoe UI", 14));
        name.setStyle("-fx-font-weight: bold;");
        name.setTextFill(javafx.scene.paint.Color.web(UiTheme.TEXT));

        chip.getChildren().addAll(hi, name);
        return chip;
    }

    private static Button navButton(String text, String key, String activeKey, Runnable onClick) {
        boolean active = key.equals(activeKey);
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setPrefHeight(38);
        b.setFont(Font.font("Segoe UI", 13));
        if (active) {
            b.setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-text-fill: %s;
                    -fx-background-radius: 8;
                    -fx-cursor: hand;
                    -fx-font-weight: bold;
                    -fx-padding: 0 12;
                    """, UiTheme.ACCENT, UiTheme.BG));
        } else {
            b.setStyle(String.format("""
                    -fx-background-color: transparent;
                    -fx-text-fill: %s;
                    -fx-background-radius: 8;
                    -fx-cursor: hand;
                    -fx-padding: 0 12;
                    """, UiTheme.TEXT_MUTED));
            b.setOnMouseEntered(e -> b.setStyle(String.format("""
                    -fx-background-color: %s;
                    -fx-text-fill: %s;
                    -fx-background-radius: 8;
                    -fx-cursor: hand;
                    -fx-padding: 0 12;
                    """, UiTheme.SURFACE, UiTheme.TEXT)));
            b.setOnMouseExited(e -> b.setStyle(String.format("""
                    -fx-background-color: transparent;
                    -fx-text-fill: %s;
                    -fx-background-radius: 8;
                    -fx-cursor: hand;
                    -fx-padding: 0 12;
                    """, UiTheme.TEXT_MUTED)));
        }
        b.setOnAction(e -> onClick.run());
        return b;
    }

    private static Button dangerButton(String text, Runnable onClick) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setPrefHeight(38);
        b.setFont(Font.font("Segoe UI", 13));
        b.setStyle(String.format("""
                -fx-background-color: transparent;
                -fx-text-fill: %s;
                -fx-background-radius: 8;
                -fx-cursor: hand;
                -fx-padding: 0 12;
                """, UiTheme.DANGER));
        b.setOnMouseEntered(e -> b.setStyle(String.format("""
                -fx-background-color: #2A1A1A;
                -fx-text-fill: %s;
                -fx-background-radius: 8;
                -fx-cursor: hand;
                -fx-padding: 0 12;
                """, UiTheme.DANGER)));
        b.setOnMouseExited(e -> b.setStyle(String.format("""
                -fx-background-color: transparent;
                -fx-text-fill: %s;
                -fx-background-radius: 8;
                -fx-cursor: hand;
                -fx-padding: 0 12;
                """, UiTheme.DANGER)));
        b.setOnAction(e -> onClick.run());
        return b;
    }
}
