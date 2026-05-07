package masroofy.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import masroofy.App;
import masroofy.model.BudgetCycle;

/**
 * SceneBase
 * Shared layout: sidebar + navbar + content area
 * All scenes extend this to get the same shell.
 */
public abstract class SceneBase {

    // ── Colors ────────────────────────────────────────────────────────────────
    protected static final String BG_DEEP    = "#0D1117";
    protected static final String BG_CARD    = "#161B22";
    protected static final String BG_SIDEBAR = "#0D1117";
    protected static final String BORDER     = "#30363D";
    protected static final String ACCENT     = "#1F6FEB";
    protected static final String TEXT_WHITE = "#E6EDF3";
    protected static final String TEXT_GRAY  = "#8B949E";
    protected static final String TEXT_DIM   = "#484F58";
    protected static final String SUCCESS    = "#3FB950";
    protected static final String WARNING    = "#D29922";
    protected static final String DANGER     = "#F85149";

    protected final Stage       stage;
    protected final BudgetCycle cycle;

    // Current user name — shared across scenes
    protected static String currentUser = "Student";

    public SceneBase(Stage stage, BudgetCycle cycle) {
        this.stage = stage;
        this.cycle = cycle;
    }

    // ── Build the full shell and inject content ───────────────────────────────
    public void show() {
        BorderPane shell = new BorderPane();
        shell.setStyle("-fx-background-color: " + BG_DEEP + ";");

        shell.setTop(buildNavbar());
        shell.setLeft(buildSidebar());
        shell.setCenter(buildContent());

        App.setContent(shell);
    }

    // ── Navbar ────────────────────────────────────────────────────────────────
    private HBox buildNavbar() {
        HBox nav = new HBox();
        nav.setPadding(new Insets(0, 20, 0, 20));
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPrefHeight(52);
        nav.setStyle(
            "-fx-background-color: " + BG_DEEP + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 0 0 1 0;"
        );

        // Logo
        Label logo = new Label("Masroofy");
        logo.setFont(Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 18));
        logo.setTextFill(Color.web(TEXT_WHITE));

        Label tagline = new Label("  student budget control");
        tagline.setFont(Font.font("Segoe UI", 12));
        tagline.setTextFill(Color.web(TEXT_DIM));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // User initial bubble
        Label userBubble = new Label(currentUser.substring(0, Math.min(2, currentUser.length())));
        userBubble.setFont(Font.font("Segoe UI", 12));
        userBubble.setTextFill(Color.web(TEXT_WHITE));
        userBubble.setStyle(
            "-fx-background-color: #30363D;" +
            "-fx-background-radius: 50;" +
            "-fx-padding: 4 8;"
        );

        // Sign out
        Button signOut = new Button("Sign Out");
        signOut.setFont(Font.font("Segoe UI", 12));
        signOut.setStyle(
            "-fx-background-color: #21262D;" +
            "-fx-text-fill: " + TEXT_WHITE + ";" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 5 12;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1;"
        );
        signOut.setOnAction(e -> {
            DashboardScene.resetSession();
            currentUser = "Student";
            new InitScene(stage).show();
        });

        // DB status badge
        Label dbBadge = new Label("Local mode · SQLite");
        dbBadge.setFont(Font.font("Segoe UI", 11));
        dbBadge.setTextFill(Color.web(TEXT_WHITE));
        dbBadge.setStyle(
            "-fx-background-color: #3D2B1F;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 4 10;"
        );

        HBox rightSide = new HBox(10, userBubble, signOut, dbBadge);
        rightSide.setAlignment(Pos.CENTER);

        nav.getChildren().addAll(logo, tagline, spacer, rightSide);
        return nav;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private VBox buildSidebar() {
        VBox sidebar = new VBox(2);
        sidebar.setPrefWidth(175);
        sidebar.setPadding(new Insets(16, 8, 16, 8));
        sidebar.setStyle(
            "-fx-background-color: " + BG_SIDEBAR + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 0 1 0 0;"
        );

        String[] labels = {"Setup", "Dashboard", "Expenses", "History", "Stats", "Settings"};
        String active = getActiveNav();

        for (String label : labels) {
            Button btn = sidebarButton(label, label.equals(active));
            btn.setOnAction(e -> navigateTo(label));
            sidebar.getChildren().add(btn);
        }

        // Spacer
        Region sp = new Region();
        VBox.setVgrow(sp, Priority.ALWAYS);
        sidebar.getChildren().add(sp);

        // Separator
        Pane sep = new Pane();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: " + BORDER + ";");
        sidebar.getChildren().add(sep);

        // Cycle info at bottom
        if (cycle != null) {
            VBox cycleInfo = new VBox(2);
            cycleInfo.setPadding(new Insets(10, 8, 4, 8));
            Label activeLbl = new Label("Active cycle");
            activeLbl.setFont(Font.font("Segoe UI", 11));
            activeLbl.setTextFill(Color.web(TEXT_DIM));
            Label amtLbl = new Label(String.format("EGP%.2f", cycle.getTotalAmount()));
            amtLbl.setFont(Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 12));
            amtLbl.setTextFill(Color.web(TEXT_GRAY));
            cycleInfo.getChildren().addAll(activeLbl, amtLbl);
            cycleInfo.setStyle(
                "-fx-background-color: #161B22;" +
                "-fx-background-radius: 6;"
            );
            cycleInfo.setPadding(new Insets(10));
            sidebar.getChildren().add(cycleInfo);
        } else {
            VBox noActive = new VBox(2);
            noActive.setPadding(new Insets(10));
            noActive.setStyle("-fx-background-color: #161B22; -fx-background-radius: 6;");
            Label na = new Label("No active cycle");
            na.setFont(Font.font("Segoe UI", 11));
            na.setTextFill(Color.web(TEXT_DIM));
            Label cr = new Label("Create one");
            cr.setFont(Font.font("Segoe UI", 11));
            cr.setTextFill(Color.web(ACCENT));
            cr.setStyle("-fx-cursor: hand;");
            cr.setOnMouseClicked(e -> new InitScene(stage).show());
            noActive.getChildren().addAll(na, cr);
            sidebar.getChildren().add(noActive);
        }

        return sidebar;
    }

    // ── Navigate from sidebar ──────────────────────────────────────────────────
    private void navigateTo(String label) {
        switch (label) {
            case "Setup"     -> new InitScene(stage).show();
            case "Dashboard" -> new DashboardScene(stage, cycle).show();
            case "Expenses"  -> new ExpenseScene(stage, cycle).show();
            case "History"   -> new HistoryScene(stage, cycle).show();
            case "Stats"     -> new StatsScene(stage, cycle).show();
            case "Settings"  -> new SettingsScene(stage, cycle).show();
        }
    }

    // ── Sidebar button ────────────────────────────────────────────────────────
    private Button sidebarButton(String text, boolean active) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setPrefHeight(36);
        b.setFont(Font.font("Segoe UI", 13));
        if (active) {
            b.setStyle(
                "-fx-background-color: " + ACCENT + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 0 12;" +
                "-fx-cursor: hand;" +
                "-fx-font-weight: bold;"
            );
        } else {
            b.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + TEXT_GRAY + ";" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 0 12;" +
                "-fx-cursor: hand;"
            );
            b.setOnMouseEntered(e -> b.setStyle(
                "-fx-background-color: #21262D;" +
                "-fx-text-fill: " + TEXT_WHITE + ";" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 0 12;" +
                "-fx-cursor: hand;"
            ));
            b.setOnMouseExited(e -> b.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + TEXT_GRAY + ";" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 0 12;" +
                "-fx-cursor: hand;"
            ));
        }
        return b;
    }

    // ── Card helper ───────────────────────────────────────────────────────────
    protected VBox card(String title) {
        VBox c = new VBox(14);
        c.setPadding(new Insets(20));
        c.setStyle(
            "-fx-background-color: " + BG_CARD + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;"
        );
        if (title != null && !title.isEmpty()) {
            Label lbl = new Label(title);
            lbl.setFont(Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 15));
            lbl.setTextFill(Color.web(TEXT_WHITE));
            c.getChildren().add(lbl);
        }
        return c;
    }

    // ── Stat box helper ───────────────────────────────────────────────────────
    protected VBox statBox(String label, String value) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(16));
        box.setStyle(
            "-fx-background-color: " + BG_CARD + ";" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1;"
        );
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", 11));
        lbl.setTextFill(Color.web(TEXT_GRAY));
        Label val = new Label(value);
        val.setFont(Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 20));
        val.setTextFill(Color.web(TEXT_WHITE));
        box.getChildren().addAll(lbl, val);
        return box;
    }

    // ── Input field helper ────────────────────────────────────────────────────
    protected javafx.scene.control.TextField inputField(String prompt, double width) {
        javafx.scene.control.TextField tf = new javafx.scene.control.TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(width);
        tf.setPrefHeight(34);
        tf.setFont(Font.font("Segoe UI", 13));
        tf.setStyle(
            "-fx-background-color: #0D1117;" +
            "-fx-text-fill: " + TEXT_WHITE + ";" +
            "-fx-prompt-text-fill: " + TEXT_DIM + ";" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 4 10;"
        );
        return tf;
    }

    // ── Primary button helper ─────────────────────────────────────────────────
    protected Button primaryButton(String text, double width) {
        Button b = new Button(text);
        b.setPrefWidth(width);
        b.setPrefHeight(38);
        b.setFont(Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 13));
        b.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: #0D1117;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        return b;
    }

    // ── Secondary button helper ───────────────────────────────────────────────
    protected Button secondaryButton(String text, double width) {
        Button b = new Button(text);
        b.setPrefWidth(width);
        b.setPrefHeight(38);
        b.setFont(Font.font("Segoe UI", 13));
        b.setStyle(
            "-fx-background-color: #21262D;" +
            "-fx-text-fill: " + TEXT_WHITE + ";" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1;" +
            "-fx-cursor: hand;"
        );
        return b;
    }

    // ── Danger button helper ──────────────────────────────────────────────────
    protected Button dangerButton(String text, double width) {
        Button b = new Button(text);
        b.setPrefWidth(width);
        b.setPrefHeight(38);
        b.setFont(Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 13));
        b.setStyle(
            "-fx-background-color: " + DANGER + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        return b;
    }

    // ── Row label helper ──────────────────────────────────────────────────────
    protected Label rowLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", 13));
        l.setTextFill(Color.web(TEXT_GRAY));
        l.setMinWidth(90);
        return l;
    }

    // ── Page title ────────────────────────────────────────────────────────────
    protected Label pageTitle(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 22));
        l.setTextFill(Color.web(TEXT_WHITE));
        return l;
    }

    // ── Error label ───────────────────────────────────────────────────────────
    protected Label errorLabel() {
        Label l = new Label("");
        l.setFont(Font.font("Segoe UI", 12));
        l.setTextFill(Color.web(DANGER));
        l.setWrapText(true);
        return l;
    }

    // ── Subclasses implement these ────────────────────────────────────────────
    protected abstract javafx.scene.Node buildContent();
    protected abstract String getActiveNav();
}