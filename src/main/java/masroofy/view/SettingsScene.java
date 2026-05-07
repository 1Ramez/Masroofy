package masroofy.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import masroofy.App;
import masroofy.controller.CycleController;
import masroofy.data.DAOLayer;
import masroofy.model.BudgetCycle;

// NOTE: We do NOT import javafx.scene.control.Alert here
// because masroofy.controller.Alert would conflict.
// We use javafx.scene.control.Alert with its full class name below.

/**
 * SettingsScene
 * SD-7: offline persistence info + cycle reset
 *
 * Bug Fix 3: removed wildcard import of javafx.scene.control.*
 * which was conflicting with masroofy.controller.Alert.
 * Now uses javafx.scene.control.Alert by full class name.
 */
public class SettingsScene {

    private final Stage           stage;
    private final BudgetCycle     cycle;
    private final CycleController cycleController;
    private final DAOLayer        daoLayer;

    public SettingsScene(Stage stage, BudgetCycle cycle) {
        this.stage           = stage;
        this.cycle           = cycle;
        this.cycleController = new CycleController();
        this.daoLayer        = new DAOLayer();
    }

    public void show() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(32));
        root.setStyle("-fx-background-color: #0D0D0D;");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Button back = new Button("← Back");
        back.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #C9A84C;
            -fx-cursor: hand;
            -fx-font-size: 13px;
            """);
        back.setOnAction(e -> new DashboardScene(stage, cycle).show());

        Label title = new Label("Settings");
        title.setFont(Font.font("Segoe UI", 22));
        title.setTextFill(Color.web("#EEEEEE"));
        header.getChildren().addAll(back, title);

        // Cycle info card
        VBox infoCard = sectionCard("Current Cycle");
        infoCard.getChildren().addAll(
            infoRow("Start Date",       cycle.getStartDate().toString()),
            infoRow("End Date",         cycle.getEndDate().toString()),
            infoRow("Total Budget",     String.format("%.2f EGP", cycle.getTotalAmount())),
            infoRow("Remaining",        String.format("%.2f EGP", cycle.getRemainingBalance())),
            infoRow("Safe Daily Limit", String.format("%.2f EGP", cycle.getSafeDailyLimit())),
            infoRow("Days Left",        cycle.getRemainingDays() + " days")
        );

        // Storage card — SD-7
        VBox storageCard = sectionCard("Storage");
        String dbPath = daoLayer.getSetting("db_path");
        if (dbPath == null) {
            dbPath = "masroofy.db (local SQLite)";
            daoLayer.saveSetting("db_path", dbPath);
        }
        Label savedLbl = new Label("✓  All data saved locally — no internet needed");
        savedLbl.setFont(Font.font("Segoe UI", 13));
        savedLbl.setTextFill(Color.web("#4CAF50"));
        Label dbLbl = new Label("Database: " + dbPath);
        dbLbl.setFont(Font.font("Segoe UI", 12));
        dbLbl.setTextFill(Color.web("#666666"));
        storageCard.getChildren().addAll(savedLbl, dbLbl);

        // Danger zone card
        VBox dangerCard = sectionCard("Danger Zone");
        Label dangerInfo = new Label(
            "Reset current cycle and all its transactions. This cannot be undone."
        );
        dangerInfo.setFont(Font.font("Segoe UI", 12));
        dangerInfo.setTextFill(Color.web("#888888"));
        dangerInfo.setWrapText(true);

        Button resetBtn = new Button("Reset Current Cycle");
        resetBtn.setPrefHeight(40);
        resetBtn.setStyle("""
            -fx-background-color: #2A1A1A;
            -fx-text-fill: #E05555;
            -fx-background-radius: 8;
            -fx-border-color: #E05555;
            -fx-border-radius: 8;
            -fx-border-width: 1;
            -fx-cursor: hand;
            -fx-font-size: 13px;
            -fx-padding: 8 20;
            """);
        resetBtn.setOnAction(e -> showResetConfirmation());
        dangerCard.getChildren().addAll(dangerInfo, resetBtn);

        root.getChildren().addAll(header, infoCard, storageCard, dangerCard);

        // Fix 1: swap content, no new Scene
        App.setContent(root);
    }

    private void showResetConfirmation() {
        // Fix 3: use full class name to avoid conflict with masroofy.controller.Alert
        javafx.scene.control.Alert dialog = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.CONFIRMATION
        );
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
                        javafx.scene.control.Alert.AlertType.ERROR
                    );
                    err.setTitle("Reset Failed");
                    err.setHeaderText(null);
                    err.setContentText("Could not reset the cycle. Please try again.");
                    err.showAndWait();
                }
            }
        });
    }

    private VBox sectionCard(String sectionTitle) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("""
            -fx-background-color: #1A1A1A;
            -fx-background-radius: 12;
            -fx-border-color: #2A2A2A;
            -fx-border-radius: 12;
            -fx-border-width: 1;
            """);

        Label lbl = new Label(sectionTitle);
        lbl.setFont(Font.font("Segoe UI", 14));
        lbl.setTextFill(Color.web("#C9A84C"));

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #2A2A2A;");

        card.getChildren().addAll(lbl, sep);
        return card;
    }

    private HBox infoRow(String key, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);

        Label keyLbl = new Label(key);
        keyLbl.setFont(Font.font("Segoe UI", 13));
        keyLbl.setTextFill(Color.web("#888888"));
        keyLbl.setMinWidth(160);

        Label valLbl = new Label(value);
        valLbl.setFont(Font.font("Segoe UI", 13));
        valLbl.setTextFill(Color.web("#EEEEEE"));

        row.getChildren().addAll(keyLbl, valLbl);
        return row;
    }
}
