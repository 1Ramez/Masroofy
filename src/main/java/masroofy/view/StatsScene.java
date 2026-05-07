package masroofy.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import masroofy.App;
import masroofy.controller.ChartGenerator;
import masroofy.model.BudgetCycle;

import java.io.File;
/**
 * Statistics view that generates and displays a spending chart for the cycle.
 */

public class StatsScene {

    private final Stage          stage;
    private final BudgetCycle    cycle;
    private final ChartGenerator chartGenerator;
/**
 * Creates the stats view.
 *
 * @param stage application stage
 * @param cycle active cycle
 */
public StatsScene(Stage stage, BudgetCycle cycle) {
        this.stage          = stage;
        this.cycle          = cycle;
        this.chartGenerator = new ChartGenerator();
    }
/**
 * Builds and displays the stats UI.
 */
public void show() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(32));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #0D0D0D;");
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
        Label title = new Label("Spending Stats");
        title.setFont(Font.font("Segoe UI", 22));
        title.setTextFill(Color.web("#EEEEEE"));
        header.getChildren().addAll(back, title);
        header.setMaxWidth(Double.MAX_VALUE);
        HBox summary = new HBox(12);
        summary.setAlignment(Pos.CENTER);
        summary.getChildren().addAll(
            statCard("Total Budget",
                String.format("%.2f EGP", cycle.getTotalAmount()),     "#C9A84C"),
            statCard("Remaining",
                String.format("%.2f EGP", cycle.getRemainingBalance()), "#4CAF50"),
            statCard("Daily Limit",
                String.format("%.2f EGP", cycle.getSafeDailyLimit()),   "#378ADD"),
            statCard("Days Left",
                cycle.getRemainingDays() + " days",                     "#9C6FD6")
        );
        VBox chartCard = new VBox(16);
        chartCard.setAlignment(Pos.CENTER);
        chartCard.setPadding(new Insets(24));
        chartCard.setMaxWidth(560);
        chartCard.setStyle("""
            -fx-background-color: #1A1A1A;
            -fx-background-radius: 12;
            -fx-border-color: #2A2A2A;
            -fx-border-radius: 12;
            -fx-border-width: 1;
            """);

        Label chartTitle = new Label("Spending by Category");
        chartTitle.setFont(Font.font("Segoe UI", 15));
        chartTitle.setTextFill(Color.web("#AAAAAA"));
        Label loadingLbl = new Label("Generating chart...");
        loadingLbl.setFont(Font.font("Segoe UI", 13));
        loadingLbl.setTextFill(Color.web("#555555"));
        chartCard.getChildren().addAll(chartTitle, loadingLbl);
        String chartPath = chartGenerator.generatePieChart(cycle.getBudgetCycleId());

        chartCard.getChildren().remove(loadingLbl);

        if (chartPath == null) {
            Label noData = new Label("No data available. Log an expense to see your insights.");
            noData.setFont(Font.font("Segoe UI", 13));
            noData.setTextFill(Color.web("#555555"));
            noData.setWrapText(true);
            chartCard.getChildren().add(noData);
        } else {
            File f = new File(chartPath);
            if (f.exists()) {
                ImageView img = new ImageView(
                    new Image(f.toURI().toString(), true)
                );
                img.setFitWidth(480);
                img.setFitHeight(340);
                img.setPreserveRatio(true);
                chartCard.getChildren().add(img);
            } else {
                Label err = new Label("Chart file not found. Is Python installed?");
                err.setTextFill(Color.web("#E05555"));
                err.setFont(Font.font("Segoe UI", 13));
                chartCard.getChildren().add(err);
            }
        }
        Button refreshBtn = new Button("↻  Refresh Chart");
        refreshBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #C9A84C;
            -fx-border-color: #C9A84C;
            -fx-border-radius: 8;
            -fx-border-width: 1;
            -fx-background-radius: 8;
            -fx-cursor: hand;
            -fx-font-size: 13px;
            -fx-padding: 8 20;
            """);
        refreshBtn.setOnAction(e -> new StatsScene(stage, cycle).show());

        root.getChildren().addAll(header, summary, chartCard, refreshBtn);
        App.setContent(root);
    }
/**
 * Creates a summary stat card.
 *
 * @param label stat label
 * @param value stat value
 * @param color value color
 * @return card container
 */
private VBox statCard(String label, String value, String color) {
        VBox c = new VBox(6);
        c.setAlignment(Pos.CENTER);
        c.setPadding(new Insets(16, 22, 16, 22));
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
}
