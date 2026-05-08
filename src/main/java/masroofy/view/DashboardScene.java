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
 * Dashboard view that displays today's remaining safe spending limit.
 *
 * <p>
 * The dashboard performs a once-per-session rollover check and provides
 * navigation to other scenes.
 * </p>
 */

public class DashboardScene {

    private final Stage stage;
    private BudgetCycle cycle;
    private static boolean rolloverDone = false;
    private static String rolloverColor = "#C9A84C";

    /**
     * Creates a dashboard scene instance.
     *
     * @param stage application stage
     * @param cycle active budget cycle
     */
    public DashboardScene(Stage stage, BudgetCycle cycle) {
        this.stage = stage;
        this.cycle = cycle;
    }

    /**
     * Builds and displays the dashboard UI.
     */
    public void show() {
        if (!rolloverDone) {
            Clock clock = new Clock();
            Clock.RolloverResult result = clock.performCheck();
            if (result != null) {
                this.cycle = result.cycle;
                rolloverColor = result.isNegative ? "#E07840" : "#4CAF50";
            }
            rolloverDone = true;
        }
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
                buildActionButtons());
        root.setCenter(center);
        App.setContent(root);
    }

    /**
     * Resets the in-memory session flags used by rollover logic.
     */
    public static void resetSession() {
        rolloverDone = false;
        rolloverColor = "#C9A84C";
    }

    /**
     * Builds the primary card showing today's remaining safe limit.
     *
     * @return limit card node
     */
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
                String.format("%.2f EGP", cycle.getRemainingDailyLimit()));
        limitLbl.setFont(Font.font("Segoe UI", 42));
        limitLbl.setTextFill(Color.web(rolloverColor));

        Label remainLbl = new Label(String.format(
                "Daily limit: %.2f EGP  ·  Cycle remaining: %.2f EGP  ·  %d days left",
                cycle.getSafeDailyLimit(),
                cycle.getRemainingBalance(),
                cycle.getRemainingDays()));
        remainLbl.setFont(Font.font("Segoe UI", 13));
        remainLbl.setTextFill(Color.web("#666666"));
        remainLbl.setWrapText(false);

        card.getChildren().addAll(titleLbl, limitLbl, remainLbl);
        return card;
    }

    /**
     * Builds the summary stats row.
     *
     * @return stats row node
     */
    private HBox buildStatsRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER);

        row.getChildren().addAll(
                miniCard("Total Budget",
                        String.format("%.0f EGP", cycle.getTotalAmount()), "#8bf859"),
                miniCard("Remaining",
                        String.format("%.0f EGP", cycle.getRemainingBalance()), "#4CAF50"),
                miniCard("Days Left",
                        cycle.getRemainingDays() + " days", "#6db0f3"));
        return row;
    }

    /**
     * Builds the action button row used for navigation.
     *
     * @return action buttons container
     */
    private HBox buildActionButtons() {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER);

        Button logBtn = actionButton("+ Log Expense", "#C9A84C", "#0D0D0D", true);
        Button historyBtn = actionButton("History", "#1E1E1E", "#CCCCCC", false);
        Button statsBtn = actionButton("Stats", "#1E1E1E", "#CCCCCC", false);
        Button settingsBtn = actionButton("Settings", "#1E1E1E", "#CCCCCC", false);

        logBtn.setOnAction(e -> new ExpenseScene(stage, cycle).show());
        historyBtn.setOnAction(e -> new HistoryScene(stage, cycle).show());
        statsBtn.setOnAction(e -> new StatsScene(stage, cycle).show());
        settingsBtn.setOnAction(e -> new SettingsScene(stage, cycle).show());

        box.getChildren().addAll(logBtn, historyBtn, statsBtn, settingsBtn);
        return box;
    }

    /**
     * Builds the dashboard navbar.
     *
     * @return navbar node
     */
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
                cycle.getStartDate() + "  →  " + cycle.getEndDate());
        info.setFont(Font.font("Segoe UI", 12));
        info.setTextFill(Color.web("#555555"));

        nav.getChildren().addAll(brand, spacer, info);
        return nav;
    }

    /**
     * Creates a compact stat card used in the stats row.
     *
     * @param label stat label
     * @param value stat value
     * @param color value color
     * @return card node
     */
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

    /**
     * Creates a styled action button.
     *
     * @param text button text
     * @param bg   background color
     * @param fg   text color
     * @param bold whether to render the label in bold
     * @return button instance
     */
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
