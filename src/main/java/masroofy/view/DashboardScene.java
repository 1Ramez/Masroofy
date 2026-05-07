package masroofy.view;

import java.time.LocalDate;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import masroofy.App;
import masroofy.controller.Clock;
import masroofy.data.DAOLayer;
import masroofy.model.BudgetCycle;

/**
 * DashboardScene
 * SD-3: display updated Safe Daily Limit
 * SD-5: rollover check once per session
 *
 * Bug Fixes:
 * 1. Uses App.setContent() — no new Scene, no minimize bug
 * 2. Chart removed from dashboard — only shown in StatsScene on demand
 * 3. Rollover only runs once per session
 */
public class DashboardScene {

    private final Stage       stage;
    private       BudgetCycle cycle;

    // Rollover runs once per app session only
    private static boolean rolloverDone  = false;
    private static String  rolloverColor = "#C9A84C";

    public DashboardScene(Stage stage, BudgetCycle cycle) {
        this.stage = stage;
        this.cycle = cycle;
    }

    public void show() {
        // SD-5: once per session only
        if (!rolloverDone) {
            Clock clock = new Clock();
            Clock.RolloverResult result = clock.performCheck();
            if (result != null) {
                this.cycle    = result.cycle;
                rolloverColor = result.isNegative ? "#E07840" : "#4CAF50";
            }
            rolloverDone = true;
        }

        // Remaining safe limit today = fixed daily limit - spent today
        DAOLayer daoLayer = new DAOLayer();
        float spentToday = daoLayer.getTotalSpentOnDate(cycle.getBudgetCycleId(), LocalDate.now());
        cycle.calculateBalance();
        float startOfDayRemaining = cycle.getRemainingBalance() + spentToday;
        float dailyLimitToday = startOfDayRemaining / cycle.getRemainingDays();
        cycle.setSafeDailyLimit(dailyLimitToday);
        cycle.setRemainingDailyLimit(dailyLimitToday - spentToday);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0D0D0D;");
        root.setTop(buildNavbar());

        VBox center = new VBox(24);
        center.setPadding(new Insets(40));
        center.setAlignment(Pos.TOP_CENTER);
        center.getChildren().addAll(
            buildLimitCard(),
            buildStatsRow(),
            buildActionButtons()
        );
        root.setCenter(center);

        // Fix 1: swap content instead of new Scene
        App.setContent(root);
    }

    public static void resetSession() {
        rolloverDone  = false;
        rolloverColor = "#C9A84C";
    }

    // ── Limit Card ────────────────────────────────────────────────────────────
    private VBox buildLimitCard() {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(32));
        card.setMaxWidth(700);
        card.setStyle("""
            -fx-background-color: #0a0a0a;
            -fx-background-radius: 16;
            -fx-border-color: #0d0d0e;
            -fx-border-radius: 16;
            -fx-border-width: 1;
            """);

        Label titleLbl = new Label("Remaining Safe Limit Today");
        titleLbl.setFont(Font.font("Segoe UI", 13));
        titleLbl.setTextFill(Color.web("#888888"));

        Label limitLbl = new Label(
            String.format("%.2f EGP", cycle.getRemainingDailyLimit())
        );
        limitLbl.setFont(Font.font("Segoe UI", 42));
        limitLbl.setTextFill(Color.web(rolloverColor));

        Label remainLbl = new Label(String.format(
            "Daily limit: %.2f EGP  ·  Cycle remaining: %.2f EGP  ·  %d days left",
            cycle.getSafeDailyLimit(),
            cycle.getRemainingBalance(),
            cycle.getRemainingDays()
        ));
        remainLbl.setFont(Font.font("Segoe UI", 13));
        remainLbl.setTextFill(Color.web("#666666"));
        remainLbl.setWrapText(false);

        card.getChildren().addAll(titleLbl, limitLbl, remainLbl);
        return card;
    }

    // ── Stats Row ─────────────────────────────────────────────────────────────
    private HBox buildStatsRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER);

        row.getChildren().addAll(
            miniCard("Total Budget",
                String.format("%.0f EGP", cycle.getTotalAmount()), "#8bf859"),
            miniCard("Remaining",
                String.format("%.0f EGP", cycle.getRemainingBalance()), "#4CAF50"),
            miniCard("Days Left",
                cycle.getRemainingDays() + " days", "#6db0f3")
        );
        return row;
    }

    // ── Action Buttons ────────────────────────────────────────────────────────
    private HBox buildActionButtons() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER);

        Button logBtn      = actionButton("+ Log Expense", "#C9A84C", "#0D0D0D", true);
        Button historyBtn  = actionButton("History",       "#1E1E1E", "#CCCCCC", false);
        Button statsBtn    = actionButton("Stats",         "#1E1E1E", "#CCCCCC", false);
        Button settingsBtn = actionButton("Settings",      "#1E1E1E", "#CCCCCC", false);

        logBtn.setOnAction(e      -> new ExpenseScene(stage, cycle).show());
        historyBtn.setOnAction(e  -> new HistoryScene(stage, cycle).show());
        // Fix 2: chart only loaded when Stats is explicitly clicked
        statsBtn.setOnAction(e    -> new StatsScene(stage, cycle).show());
        settingsBtn.setOnAction(e -> new SettingsScene(stage, cycle).show());

        box.getChildren().addAll(logBtn, historyBtn, statsBtn, settingsBtn);
        return box;
    }

    // ── Navbar ────────────────────────────────────────────────────────────────
    private HBox buildNavbar() {
        HBox nav = new HBox();
        nav.setPadding(new Insets(16, 24, 16, 24));
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setStyle("""
            -fx-background-color: #141414;
            -fx-border-color: #222222;
            -fx-border-width: 0 0 1 0;
            """);

        Label brand = new Label("Masroofy");
        brand.setFont(Font.font("Segoe UI", 20));
        brand.setTextFill(Color.web("#C9A84C"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label info = new Label(
            cycle.getStartDate() + "  →  " + cycle.getEndDate()
        );
        info.setFont(Font.font("Segoe UI", 12));
        info.setTextFill(Color.web("#555555"));

        nav.getChildren().addAll(brand, spacer, info);
        return nav;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private VBox miniCard(String label, String value, String color) {
        VBox c = new VBox(4);
        c.setAlignment(Pos.CENTER);
        c.setPadding(new Insets(14, 20, 14, 20));
        c.setStyle("""
            -fx-background-color: #1A1A1A;
            -fx-background-radius: 10;
            -fx-border-color: #2A2A2A;
            -fx-border-radius: 10;
            -fx-border-width: 1;
            """);
        Label v = new Label(value);
        v.setFont(Font.font("Segoe UI", 18));
        v.setTextFill(Color.web(color));
        Label l = new Label(label);
        l.setFont(Font.font("Segoe UI", 11));
        l.setTextFill(Color.web("#555555"));
        c.getChildren().addAll(v, l);
        return c;
    }

    private Button actionButton(String text, String bg, String fg, boolean bold) {
        Button b = new Button(text);
        b.setPrefHeight(42);
        b.setPrefWidth(138);
        b.setFont(Font.font("Segoe UI", 13));
        b.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            %s
            """, bg, fg, bold ? "-fx-font-weight: bold;" : ""));
        return b;
    }
}
