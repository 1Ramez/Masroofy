package masroofy.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import masroofy.App;
import masroofy.data.DAOLayer;
import masroofy.model.BudgetCycle;
import masroofy.model.Expense;

import java.util.List;

/**
 * HistoryScene
 * Shows all transactions for the current cycle.
 * Fix: uses App.setContent() — no minimize bug
 */
public class HistoryScene {

    private final Stage       stage;
    private final BudgetCycle cycle;
    private final DAOLayer    daoLayer;

    public HistoryScene(Stage stage, BudgetCycle cycle) {
        this.stage    = stage;
        this.cycle    = cycle;
        this.daoLayer = new DAOLayer();
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

        Label title = new Label("Transaction History");
        title.setFont(Font.font("Segoe UI", 22));
        title.setTextFill(Color.web("#EEEEEE"));
        header.getChildren().addAll(back, title);

        // Table
        TableView<Expense> table = new TableView<>();
        table.setStyle("""
            -fx-background-color: #1A1A1A;
            -fx-control-inner-background: #1A1A1A;
            -fx-table-cell-border-color: #2A2A2A;
            """);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Expense, Integer> idCol   = col("ID",           "expenseId",   60);
        TableColumn<Expense, Float>   amtCol  = col("Amount (EGP)", "amount",      120);
        TableColumn<Expense, Integer> catCol  = col("Category ID",  "categoryId",  100);
        TableColumn<Expense, String>  dateCol = col("Date",         "date",        140);
        TableColumn<Expense, String>  noteCol = col("Note",         "note",        200);

        table.getColumns().addAll(idCol, amtCol, catCol, dateCol, noteCol);

        List<Expense> expenses = daoLayer.getAllExpenses(cycle.getBudgetCycleId());

        if (expenses.isEmpty()) {
            Label empty = new Label("No transactions yet. Log your first expense.");
            empty.setFont(Font.font("Segoe UI", 14));
            empty.setTextFill(Color.web("#555555"));
            root.getChildren().addAll(header, empty);
        } else {
            table.getItems().addAll(expenses);
            root.getChildren().addAll(header, table);
        }

        App.setContent(root);
    }

    @SuppressWarnings("unchecked")
    private <T, S> TableColumn<T, S> col(String title, String property, double width) {
        TableColumn<T, S> c = new TableColumn<>(title);
        c.setCellValueFactory(new PropertyValueFactory<>(property));
        c.setPrefWidth(width);
        c.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 12px;");
        return c;
    }
}
