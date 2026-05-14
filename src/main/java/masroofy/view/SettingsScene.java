package masroofy.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import masroofy.App;
import masroofy.controller.CycleController;
import masroofy.controller.ThemeController;
import masroofy.data.DAOLayer;
import masroofy.model.BudgetCycle;

/**
 * Settings view showing cycle details, storage status, and reset actions.
 */

public class SettingsScene {

    private final Stage stage;
    private final BudgetCycle cycle;
    private final CycleController cycleController;
    private final DAOLayer daoLayer;

    /**
     * Creates the settings view.
     *
     * @param stage application stage
     * @param cycle active cycle
     */
    public SettingsScene(Stage stage, BudgetCycle cycle) {
        this.stage = stage;
        this.cycle = cycle;
        this.cycleController = new CycleController();
        this.daoLayer = new DAOLayer();
    }

    /**
     * Builds and displays the settings UI.
     */
    public void show() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(32));
        root.setStyle("-fx-background-color: " + UiTheme.BG + ";");
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Button back = new Button("← Back");
        back.setStyle(String.format("""
                -fx-background-color: transparent;
                -fx-text-fill: %s;
                -fx-cursor: hand;
                -fx-font-size: 13px;
                """, UiTheme.ACCENT));
        back.setOnAction(e -> new DashboardScene(stage, cycle).show());

        Label title = new Label("Settings");
        title.setFont(Font.font("Segoe UI", 22));
        title.setTextFill(Color.web(UiTheme.TEXT));
        header.getChildren().addAll(back, title);
        VBox infoCard = sectionCard("Current Cycle");
        infoCard.getChildren().addAll(
                infoRow("Start Date", cycle.getStartDate().toString()),
                infoRow("End Date", cycle.getEndDate().toString()),
                infoRow("Total Budget", String.format("%.2f EGP", cycle.getTotalAmount())),
                infoRow("Remaining", String.format("%.2f EGP", cycle.getRemainingBalance())),
                infoRow("Safe Daily Limit", String.format("%.2f EGP", cycle.getSafeDailyLimit())),
                infoRow("Days Left", cycle.getRemainingDays() + " days"));
        VBox storageCard = sectionCard("Storage");
        String dbPath = daoLayer.getSetting("db_path");
        if (dbPath == null) {
            dbPath = "masroofy.db (local SQLite)";
            daoLayer.saveSetting("db_path", dbPath);
        }
        Label savedLbl = new Label("✓  All data saved locally — no internet needed");
        savedLbl.setFont(Font.font("Segoe UI", 13));
        savedLbl.setTextFill(Color.web(UiTheme.SUCCESS));
        Label dbLbl = new Label("Database: " + dbPath);
        dbLbl.setFont(Font.font("Segoe UI", 12));
        dbLbl.setTextFill(Color.web(UiTheme.TEXT_DIM));
        storageCard.getChildren().addAll(savedLbl, dbLbl);

        VBox appearanceCard = sectionCard("Appearance");
        ThemeController themeController = new ThemeController();
        String currentMode = UiTheme.isLight() ? "Light" : "Dark";
        Label modeLbl = new Label("Theme: " + currentMode);
        modeLbl.setFont(Font.font("Segoe UI", 13));
        modeLbl.setTextFill(Color.web(UiTheme.TEXT_MUTED));

        Button toggleTheme = new Button(UiTheme.isLight() ? "Switch to Dark" : "Switch to Light");
        toggleTheme.setPrefHeight(38);
        toggleTheme.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-background-radius: 8;
                -fx-border-color: %s;
                -fx-border-radius: 8;
                -fx-border-width: 1;
                -fx-cursor: hand;
                -fx-padding: 8 16;
                """, UiTheme.SURFACE_2, UiTheme.TEXT, UiTheme.BORDER));
        toggleTheme.setOnAction(e -> {
            themeController.saveAndApply(
                    UiTheme.isLight() ? ThemeController.Mode.DARK : ThemeController.Mode.LIGHT);
            new SettingsScene(stage, cycle).show();
        });
        appearanceCard.getChildren().addAll(modeLbl, toggleTheme);
        VBox dangerCard = sectionCard("Danger Zone");
        Label dangerInfo = new Label(
                "Reset current cycle and all its transactions. This cannot be undone.");
        dangerInfo.setFont(Font.font("Segoe UI", 12));
        dangerInfo.setTextFill(Color.web(UiTheme.TEXT_MUTED));
        dangerInfo.setWrapText(true);

        Button resetBtn = new Button("Reset Current Cycle");
        resetBtn.setPrefHeight(40);
        resetBtn.setStyle(String.format("""
                -fx-background-color: #2A1A1A;
                -fx-text-fill: %s;
                -fx-background-radius: 8;
                -fx-border-color: %s;
                -fx-border-radius: 8;
                -fx-border-width: 1;
                -fx-cursor: hand;
                -fx-font-size: 13px;
                -fx-padding: 8 20;
                """, UiTheme.DANGER, UiTheme.DANGER));
        resetBtn.setOnAction(e -> showResetConfirmation());
        dangerCard.getChildren().addAll(dangerInfo, resetBtn);

        root.getChildren().addAll(header, infoCard, storageCard, appearanceCard, dangerCard);

        BorderPane shell = new BorderPane();
        shell.setStyle("-fx-background-color: " + UiTheme.BG + ";");
        shell.setLeft(Sidebar.build(stage, cycle, "settings"));
        shell.setCenter(root);
        App.setContent(shell);
    }

    /**
     * Shows a confirmation dialog and, if accepted, resets the current cycle.
     */
    private void showResetConfirmation() {
        javafx.scene.control.Alert dialog = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Reset Cycle");
        dialog.setHeaderText("This will permanently delete all logs for this cycle.");
        dialog.setContentText("Are you sure you want to reset?");

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = cycleController.resetCycle(cycle.getBudgetCycleId());
                if (success) {
                    DashboardScene.resetSession();
                    new InitScene(stage).show();
                } else {
                    javafx.scene.control.Alert err = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR);
                    err.setTitle("Reset Failed");
                    err.setHeaderText(null);
                    err.setContentText("Could not reset the cycle. Please try again.");
                    err.showAndWait();
                }
            }
        });
    }

    /**
     * Creates a styled settings section card.
     *
     * @param sectionTitle section title
     * @return card container
     */
    private VBox sectionCard(String sectionTitle) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 12;
                -fx-border-color: %s;
                -fx-border-radius: 12;
                -fx-border-width: 1;
                """, UiTheme.SURFACE, UiTheme.BORDER));

        Label lbl = new Label(sectionTitle);
        lbl.setFont(Font.font("Segoe UI", 14));
        lbl.setTextFill(Color.web(UiTheme.ACCENT));

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: " + UiTheme.BORDER + ";");

        card.getChildren().addAll(lbl, sep);
        return card;
    }

    /**
     * Creates a key/value row for cycle information display.
     *
     * @param key   label
     * @param value value
     * @return row container
     */
    private HBox infoRow(String key, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label keyLbl = new Label(key);
        keyLbl.setFont(Font.font("Segoe UI", 13));
        keyLbl.setTextFill(Color.web(UiTheme.TEXT_MUTED));
        keyLbl.setMinWidth(160);

        Label valLbl = new Label(value);
        valLbl.setFont(Font.font("Segoe UI", 13));
        valLbl.setTextFill(Color.web(UiTheme.TEXT));

        row.getChildren().addAll(keyLbl, valLbl);
        return row;
    }
}
