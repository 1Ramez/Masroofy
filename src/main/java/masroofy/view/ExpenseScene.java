package masroofy.view;

import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import masroofy.App;
import masroofy.controller.Alert;
import masroofy.controller.ExpenseController;
import masroofy.model.BudgetCycle;
import masroofy.model.Category;
import masroofy.model.Expense;

/**
 * Expense logging view that lets the user add a new transaction.
 */

public class ExpenseScene {

    private final Stage stage;
    private final BudgetCycle cycle;
    private final ExpenseController controller;
    private final Alert alert;

    /**
     * Creates the expense logging view.
     *
     * @param stage application stage
     * @param cycle active cycle
     */
    public ExpenseScene(Stage stage, BudgetCycle cycle) {
        this.stage = stage;
        this.cycle = cycle;
        this.controller = new ExpenseController();
        this.alert = new Alert();
    }

    /**
     * Builds and displays the expense logging UI.
     */
    public void show() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #0D0D0D;");
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
        Label hintLbl = new Label(
                String.format("Available: %.2f EGP", cycle.getRemainingBalance()));
        hintLbl.setFont(Font.font("Segoe UI", 13));
        hintLbl.setTextFill(Color.web("#4CAF50"));
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
        Label amountLbl = fieldLabel("Amount (EGP)");
        TextField amountField = inputField("e.g. 50");
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
        if (!categories.isEmpty())
            catBox.setValue(categories.get(0));
        Label noteLbl = fieldLabel("Note (optional)");
        TextField noteField = inputField("e.g. lunch");
        Label errorLbl = new Label("");
        errorLbl.setTextFill(Color.web("#E05555"));
        errorLbl.setFont(Font.font("Segoe UI", 12));
        errorLbl.setWrapText(true);
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
                if (amount <= 0) {
                    errorLbl.setText("Please enter a valid number");
                    return;
                }

                if (cat == null) {
                    errorLbl.setText("Please select a category.");
                    return;
                }
                if (amount > cycle.getRemainingBalance()) {
                    errorLbl.setText(String.format(
                            "Amount exceeds your remaining balance of %.2f EGP",
                            cycle.getRemainingBalance()));
                    return;
                }

                String note = noteField.getText().trim();
                Expense expense = controller.addExpense(amount, cat.getCategoryId(), note);

                if (expense == null) {
                    errorLbl.setText(controller.getValidationError());
                } else {
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
                amountLbl, amountField,
                catLbl, catBox,
                noteLbl, noteField,
                errorLbl, saveBtn);

        root.getChildren().addAll(header, hintLbl, card);
        App.setContent(root);
    }

    /**
     * Creates a styled label used for form fields.
     *
     * @param text label text
     * @return label instance
     */
    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", 13));
        l.setTextFill(Color.web("#AAAAAA"));
        return l;
    }

    /**
     * Creates a styled text field.
     *
     * @param prompt prompt text
     * @return text field instance
     */
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
