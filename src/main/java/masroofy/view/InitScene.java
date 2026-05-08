package masroofy.view;

import java.time.LocalDate;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import masroofy.App;
import masroofy.controller.CycleController;
import masroofy.model.BudgetCycle;

/**
 * Initial setup view used to create a new budget cycle.
 */

public class InitScene {

    private final Stage stage;
    private final CycleController controller;

    /**
     * Creates the setup view.
     *
     * @param stage application stage
     */
    public InitScene(Stage stage) {
        this.stage = stage;
        this.controller = new CycleController();
    }

    /**
     * Builds and displays the setup UI.
     */
    public void show() {
        VBox root = new VBox(24);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(48));
        root.setStyle("-fx-background-color: #0D0D0D;");
        Label title = new Label("Masroofy");
        title.setFont(Font.font("Segoe UI", 36));
        title.setTextFill(Color.web("#C9A84C"));

        Label subtitle = new Label("Set up your budget cycle");
        subtitle.setFont(Font.font("Segoe UI", 16));
        subtitle.setTextFill(Color.web("#888888"));
        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(32));
        card.setMaxWidth(440);
        card.setStyle("""
                -fx-background-color: #1A1A1A;
                -fx-background-radius: 12;
                -fx-border-color: #2A2A2A;
                -fx-border-radius: 12;
                -fx-border-width: 1;
                """);

        Label amountLbl = fieldLabel("Total Budget (EGP)");
        TextField amountField = inputField("e.g. 3000");

        Label startLbl = fieldLabel("Start Date");
        DatePicker startPicker = styledDatePicker();
        startPicker.setValue(LocalDate.now());

        Label endLbl = fieldLabel("End Date");
        DatePicker endPicker = styledDatePicker();
        endPicker.setValue(LocalDate.now().plusDays(30));

        Label errorLbl = new Label("");
        errorLbl.setTextFill(Color.web("#E05555"));
        errorLbl.setFont(Font.font("Segoe UI", 12));
        errorLbl.setWrapText(true);

        Button btn = new Button("Start Cycle");
        btn.setPrefWidth(376);
        btn.setPrefHeight(44);
        btn.setFont(Font.font("Segoe UI", 14));
        btn.setStyle("""
                -fx-background-color: #C9A84C;
                -fx-text-fill: #0D0D0D;
                -fx-background-radius: 8;
                -fx-font-weight: bold;
                -fx-cursor: hand;
                """);
        btn.setOnAction(e -> {
            errorLbl.setText("");
            try {
                float amount = Float.parseFloat(amountField.getText().trim());
                LocalDate start = startPicker.getValue();
                LocalDate end = endPicker.getValue();

                BudgetCycle cycle = controller.createCycle(amount, start, end);

                if (cycle == null) {
                    errorLbl.setText(controller.getValidationError());
                } else {
                    new DashboardScene(stage, cycle).show();
                }
            } catch (NumberFormatException ex) {
                errorLbl.setText("Allowance must be a positive number");
            }
        });

        card.getChildren().addAll(
                amountLbl, amountField,
                startLbl, startPicker,
                endLbl, endPicker,
                errorLbl, btn);

        root.getChildren().addAll(title, subtitle, card);
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

    /**
     * Creates a styled date picker.
     *
     * @return date picker instance
     */
    private DatePicker styledDatePicker() {
        DatePicker dp = new DatePicker();
        dp.setPrefHeight(40);
        dp.setPrefWidth(376);
        dp.setStyle("-fx-background-color: #252525; -fx-background-radius: 6;");
        dp.getEditor().setStyle("""
                -fx-background-color: #252525;
                -fx-text-fill: #EEEEEE;
                -fx-font-size: 13px;
                """);
        return dp;
    }
}
