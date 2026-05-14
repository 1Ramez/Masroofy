package masroofy.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
 * Login / Register screen.
 */
public class AuthScene {

    private enum Mode {
        LOGIN,
        REGISTER
    }

    private final Stage stage;
    private final AuthController controller;

    public AuthScene(Stage stage) {
        this.stage = stage;
        this.controller = new AuthController();
    }

    public void show() {
        VBox root = new VBox(22);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(48));
        root.setStyle("-fx-background-color: " + UiTheme.BG + ";");

        HBox top = new HBox();
        top.setMaxWidth(460);
        top.setAlignment(Pos.CENTER_RIGHT);
        Button themeBtn = new Button(UiTheme.isLight() ? "Dark mode" : "Light mode");
        themeBtn.setFont(Font.font("Segoe UI", 12));
        themeBtn.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-background-radius: 8;
                -fx-border-color: %s;
                -fx-border-radius: 8;
                -fx-border-width: 1;
                -fx-cursor: hand;
                -fx-padding: 6 12;
                """, UiTheme.SURFACE, UiTheme.TEXT_MUTED, UiTheme.BORDER));
        themeBtn.setOnAction(e -> {
            ThemeController tc = new ThemeController();
            tc.saveAndApply(UiTheme.isLight() ? ThemeController.Mode.DARK : ThemeController.Mode.LIGHT);
            new AuthScene(stage).show();
        });
        top.getChildren().add(themeBtn);

        Label title = new Label("Masroofy");
        title.setFont(Font.font("Segoe UI", 36));
        title.setTextFill(Color.web(UiTheme.ACCENT));

        Label subtitle = new Label("Log in to load your saved progress");
        subtitle.setFont(Font.font("Segoe UI", 15));
        subtitle.setTextFill(Color.web(UiTheme.TEXT_MUTED));

        VBox card = new VBox(16);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(26));
        card.setMaxWidth(460);
        card.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-background-radius: 12;
                -fx-border-color: %s;
                -fx-border-radius: 12;
                -fx-border-width: 1;
                """, UiTheme.SURFACE, UiTheme.BORDER));

        HBox tabs = new HBox(8);
        tabs.setAlignment(Pos.CENTER);

        Button loginTab = tabButton("Login", true);
        Button registerTab = tabButton("Register", false);

        VBox form = new VBox(14);
        form.setAlignment(Pos.TOP_LEFT);
        form.setPadding(new Insets(12, 0, 0, 0));

        Label errorLbl = new Label("");
        errorLbl.setTextFill(Color.web(UiTheme.DANGER));
        errorLbl.setFont(Font.font("Segoe UI", 12));
        errorLbl.setWrapText(true);

        Runnable showLogin = () -> buildLoginForm(form, errorLbl);
        Runnable showRegister = () -> buildRegisterForm(form, errorLbl);

        final Mode[] mode = new Mode[] { Mode.LOGIN };
        showLogin.run();

        loginTab.setOnAction(e -> {
            mode[0] = Mode.LOGIN;
            styleTab(loginTab, true);
            styleTab(registerTab, false);
            showLogin.run();
            errorLbl.setText("");
        });
        registerTab.setOnAction(e -> {
            mode[0] = Mode.REGISTER;
            styleTab(loginTab, false);
            styleTab(registerTab, true);
            showRegister.run();
            errorLbl.setText("");
        });

        tabs.getChildren().addAll(loginTab, registerTab);
        card.getChildren().addAll(tabs, form, errorLbl);

        root.getChildren().addAll(top, title, subtitle, card);
        App.setContent(root);
    }

    private void buildLoginForm(VBox form, Label errorLbl) {
        form.getChildren().clear();

        Label nameLbl = fieldLabel("Name");
        TextField nameField = inputField("e.g. Sara");

        Label pinLbl = fieldLabel("PIN");
        PasswordField pinField = pinField("4+ digits");

        Button loginBtn = primaryButton("Login");
        loginBtn.setOnAction(e -> {
            errorLbl.setText("");
            var user = controller.login(nameField.getText(), pinField.getText());
            if (user == null) {
                errorLbl.setText(controller.getValidationError());
                return;
            }
            DashboardScene.resetSession();
            goToMain();
        });

        form.getChildren().addAll(nameLbl, nameField, pinLbl, pinField, loginBtn);
    }

    private void buildRegisterForm(VBox form, Label errorLbl) {
        form.getChildren().clear();

        Label nameLbl = fieldLabel("Name");
        TextField nameField = inputField("e.g. Sara");

        Label pinLbl = fieldLabel("PIN");
        PasswordField pinField = pinField("choose a PIN");

        Label confirmLbl = fieldLabel("Confirm PIN");
        PasswordField confirmField = pinField("re-enter PIN");

        Button registerBtn = primaryButton("Create Account");
        registerBtn.setOnAction(e -> {
            errorLbl.setText("");
            var user = controller.register(nameField.getText(), pinField.getText(), confirmField.getText());
            if (user == null) {
                errorLbl.setText(controller.getValidationError());
                return;
            }
            DashboardScene.resetSession();
            goToMain();
        });

        form.getChildren().addAll(nameLbl, nameField, pinLbl, pinField, confirmLbl, confirmField, registerBtn);
    }

    private void goToMain() {
        CycleController cc = new CycleController();
        BudgetCycle active = cc.checkActiveCycle();
        if (active == null) {
            new InitScene(stage).show();
        } else {
            new DashboardScene(stage, active).show();
        }
    }

    private Button tabButton(String text, boolean active) {
        Button b = new Button(text);
        b.setPrefHeight(36);
        b.setPrefWidth(196);
        b.setFont(Font.font("Segoe UI", 13));
        styleTab(b, active);
        return b;
    }

    private void styleTab(Button b, boolean active) {
        b.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-background-radius: 8;
                -fx-border-color: %s;
                -fx-border-radius: 8;
                -fx-border-width: 1;
                -fx-cursor: hand;
                """,
                active ? UiTheme.ACCENT : UiTheme.SURFACE_2,
                active ? UiTheme.BG : UiTheme.TEXT_MUTED,
                UiTheme.BORDER));
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", 13));
        l.setTextFill(Color.web(UiTheme.TEXT_MUTED));
        return l;
    }

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

    private PasswordField pinField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setPrefHeight(40);
        pf.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-prompt-text-fill: %s;
                -fx-background-radius: 6;
                -fx-border-color: %s;
                -fx-border-radius: 6;
                -fx-padding: 8 12;
                """, UiTheme.SURFACE_2, UiTheme.TEXT, UiTheme.TEXT_DIM, UiTheme.BORDER));
        return pf;
    }

    private Button primaryButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(408);
        btn.setPrefHeight(44);
        btn.setFont(Font.font("Segoe UI", 14));
        btn.setStyle(String.format("""
                -fx-background-color: %s;
                -fx-text-fill: %s;
                -fx-background-radius: 8;
                -fx-font-weight: bold;
                -fx-cursor: hand;
                """, UiTheme.ACCENT, UiTheme.BG));
        return btn;
    }
}
