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
import masroofy.controller.AuthController;
import masroofy.controller.CycleController;
import masroofy.controller.ThemeController;
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
        root.setStyle("-fx-background-color: " + UiTheme.BG + ";");

        Button themeBtn = new Button(UiTheme.isLight() ? "Dark mode" : "Light mode");
        themeBtn.setFont(Font.font("Segoe UI", 12));
        themeBtn.setStyle(String.format("""
                -fx-background-color: transparent;
                -fx-text-fill: %s;
                -fx-cursor: hand;
                """, UiTheme.TEXT_MUTED));
        themeBtn.setOnAction(e -> {
            ThemeController tc = new ThemeController();
            tc.saveAndApply(UiTheme.isLight() ? ThemeController.Mode.DARK : ThemeController.Mode.LIGHT);
            new InitScene(stage).show();
        });

        Label title = new Label("Masroofy");
        title.setFont(Font.font("Segoe UI", 36));
        title.setTextFill(Color.web(UiTheme.ACCENT));

        Label subtitle = new Label("Set up your budget cycle");
        subtitle.setFont(Font.font("Segoe UI", 16));
        subtitle.setTextFill(Color.web(UiTheme.TEXT_MUTED));
        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(32));
        card.setMaxWidth(440);
        card.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 12;
                -fx-border-color: %s;
                -fx-border-radius: 12;
                -fx-border-width: 1;
                """, UiTheme.SURFACE, UiTheme.BORDER));

        Label amountLbl = fieldLabel("Total Budget (EGP)");
        TextField amountField = inputField("e.g. 3000");

        Label startLbl = fieldLabel("Start Date");
        DatePicker startPicker = styledDatePicker();
        startPicker.setValue(LocalDate.now());

        Label endLbl = fieldLabel("End Date");
        DatePicker endPicker = styledDatePicker();
        endPicker.setValue(LocalDate.now().plusDays(30));

        Label errorLbl = new Label("");
        errorLbl.setTextFill(Color.web(UiTheme.DANGER));
        errorLbl.setFont(Font.font("Segoe UI", 12));
        errorLbl.setWrapText(true);

        Button btn = new Button("Start Cycle");
        btn.setPrefWidth(376);
        btn.setPrefHeight(44);
        btn.setFont(Font.font("Segoe UI", 14));
        btn.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-background-radius: 8;
                -fx-font-weight: bold;
                -fx-cursor: hand;
                """, UiTheme.ACCENT, UiTheme.BG));
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

        Button switchAccount = new Button("Switch account");
        switchAccount.setFont(Font.font("Segoe UI", 12));
        switchAccount.setStyle(String.format("""
                -fx-background-color: transparent;
                -fx-text-fill: %s;
                -fx-cursor: hand;
                """, UiTheme.ACCENT));
        switchAccount.setOnAction(e -> {
            new AuthController().signOut();
            DashboardScene.resetSession();
            new AuthScene(stage).show();
        });

        root.getChildren().addAll(themeBtn, title, subtitle, card, switchAccount);
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
        l.setTextFill(Color.web(UiTheme.TEXT_MUTED));
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
        tf.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-prompt-text-fill: %s;
                -fx-background-radius: 6;
                -fx-border-color: %s;
                -fx-border-radius: 6;
                -fx-padding: 8 12;
                """, UiTheme.SURFACE_2, UiTheme.TEXT, UiTheme.TEXT_DIM, UiTheme.BORDER));
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
        dp.setStyle(String.format("-fx-background-color: %s; -fx-background-radius: 6;", UiTheme.SURFACE_2));
        dp.getEditor().setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-font-size: 13px;
                """, UiTheme.SURFACE_2, UiTheme.TEXT));
        return dp;
    }
}
