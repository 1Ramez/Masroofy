package masroofy.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import masroofy.App;
import masroofy.controller.Alert;
import masroofy.controller.ExpenseController;
import masroofy.model.BudgetCycle;
import masroofy.model.Category;
import masroofy.model.Expense;

import java.util.List;

/**
 * ExpenseScene
 * SD-2: addExpense(amount, category)
 *
 * Bug Fixes:
 * 1. Uses App.setContent() — no minimize bug
 * 4. Shows error if expense exceeds remaining balance
 */
public class ExpenseScene {

    private final Stage             stage;
    private final BudgetCycle       cycle;
    private final ExpenseController controller;
    private final Alert             alert;

    public ExpenseScene(Stage stage, BudgetCycle cycle) {
        this.stage      = stage;
        this.cycle      = cycle;
        this.controller = new ExpenseController();
        this.alert      = new Alert();
    }

    public void show() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #0D0D0D;");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Button backBtn = new Button("← Back");
        backBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #C9A84C;
            -fx-cursor: hand;
            -fx-font-size: 13px;
            """);
        backBtn.setOnAction(e -> new DashboardScene(stage, cycle).show());

        Label title = new Label("Log Expense");
        title.setFont(Font.font("Segoe UI", 24));
        title.setTextFill(Color.web("#EEEEEE"));
        header.getChildren().addAll(backBtn, title);

        // Remaining balance hint
        Label hintLbl = new Label(
            String.format("Available: %.2f EGP", cycle.getRemainingBalance())
        );
        hintLbl.setFont(Font.font("Segoe UI", 13));
        hintLbl.setTextFill(Color.web("#4CAF50"));

        // Card
        VBox card = new VBox(16);
        card.setPadding(new Insets(28));
        card.setMaxWidth(440);
        card.setStyle("""
            -fx-background-color: #1A1A1A;
            -fx-background-radius: 12;
            -fx-border-color: #2A2A2A;
            -fx-border-radius: 12;
            -fx-border-width: 1;
            """);

        // Amount
        Label amountLbl   = fieldLabel("Amount (EGP)");
        TextField amountField = inputField("e.g. 50");

        // Categories
        Label catLbl = fieldLabel("Category");
        ComboBox<Category> catBox = new ComboBox<>();
        catBox.setPrefHeight(40);
        catBox.setPrefWidth(384);
        catBox.setStyle("""
            -fx-background-color: #252525;
            -fx-background-radius: 6;
            """);
        List<Category> categories = controller.getCategories();
        catBox.getItems().addAll(categories);
        if (!categories.isEmpty()) catBox.setValue(categories.get(0));

        // Note
        Label noteLbl     = fieldLabel("Note (optional)");
        TextField noteField = inputField("e.g. lunch");

        // Error label
        Label errorLbl = new Label("");
        errorLbl.setTextFill(Color.web("#E05555"));
        errorLbl.setFont(Font.font("Segoe UI", 12));
        errorLbl.setWrapText(true);

        // Save button
        Button saveBtn = new Button("Save Expense");
        saveBtn.setPrefWidth(384);
        saveBtn.setPrefHeight(44);
        saveBtn.setFont(Font.font("Segoe UI", 14));
        saveBtn.setStyle("""
            -fx-background-color: #C9A84C;
            -fx-text-fill: #0D0D0D;
            -fx-background-radius: 8;
            -fx-font-weight: bold;
            -fx-cursor: hand;
            """);

        saveBtn.setOnAction(e -> {
            errorLbl.setText("");
            try {
                float amount = Float.parseFloat(amountField.getText().trim());
                Category cat = catBox.getValue();

                // Basic validation
                if (amount <= 0) {
                    errorLbl.setText("Please enter a valid number");
                    return;
                }

                if (cat == null) {
                    errorLbl.setText("Please select a category.");
                    return;
                }

                // Bug Fix 4: prevent negative balance
                if (amount > cycle.getRemainingBalance()) {
                    errorLbl.setText(String.format(
                        "Amount exceeds your remaining balance of %.2f EGP",
                        cycle.getRemainingBalance()
                    ));
                    return;
                }

                String note = noteField.getText().trim();

                // SD-2: addExpense → UpdateBalance → INSERT INTO Transactions
                Expense expense = controller.addExpense(amount, cat.getCategoryId(), note);

                if (expense == null) {
                    errorLbl.setText(controller.getValidationError());
                } else {
                    // SD-6: check spending alerts
                    BudgetCycle updated = controller.refreshDashboard();
                    if (updated != null) {
                        alert.checkSpending(updated);
                        new DashboardScene(stage, updated).show();
                    } else {
                        new DashboardScene(stage, cycle).show();
                    }
                }

            } catch (NumberFormatException ex) {
                errorLbl.setText("Please enter a valid number");
            }
        });

        card.getChildren().addAll(
            amountLbl,   amountField,
            catLbl,      catBox,
            noteLbl,     noteField,
            errorLbl,    saveBtn
        );

        root.getChildren().addAll(header, hintLbl, card);

        // Fix 1: swap content, no new Scene
        App.setContent(root);
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", 13));
        l.setTextFill(Color.web("#AAAAAA"));
        return l;
    }

    private TextField inputField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefHeight(40);
        tf.setStyle("""
            -fx-background-color: #252525;
            -fx-text-fill: #EEEEEE;
            -fx-prompt-text-fill: #555555;
            -fx-background-radius: 6;
            -fx-border-color: #333333;
            -fx-border-radius: 6;
            -fx-padding: 8 12;
            """);
        return tf;
    }
}
