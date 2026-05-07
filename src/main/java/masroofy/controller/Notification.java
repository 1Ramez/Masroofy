package masroofy.controller;

import javafx.application.Platform;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Notification
 * MVC Role  : Controller
 * Class Diag: notifId, currentTitle, type, msg
 *             send() : void, maintenance() : void
 *
 * SD-6 Step 7: send() : void
 * Sends a desktop OS notification using Java AWT SystemTray.
 * Matches architecture diagram: Notification Service [Java AWT SystemTray]
 */
public class Notification {

    private int    notifId;
    private String currentTitle;
    private String type;
    private String msg;

    public enum Level { WARNING, ERROR }

    private static TrayIcon trayIcon;
    private static boolean trayInitAttempted = false;

    public Notification() {
        this.notifId = 0;
    }

    // SD-6 Step 7: send() : void
    public void send(String message) {
        send(message, Level.WARNING);
    }

    public void send(String message, Level level) {
        this.msg          = message;
        this.currentTitle = "Masroofy Alert";
        this.notifId++;

        if (trySendSystemTray(message, level)) return;

        // Fallback: in-app JavaFX alert (always visible)
        try {
            Platform.runLater(() -> {
                javafx.scene.control.Alert.AlertType alertType =
                    (level == Level.ERROR)
                        ? javafx.scene.control.Alert.AlertType.ERROR
                        : javafx.scene.control.Alert.AlertType.WARNING;

                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(alertType);
                alert.setTitle(currentTitle);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.show();
            });
        } catch (IllegalStateException ex) {
            // JavaFX Platform not initialized — last resort log
            System.out.println("[Notification] Message: " + message);
        }
    }

    private boolean trySendSystemTray(String message, Level level) {
        if (!SystemTray.isSupported()) return false;

        try {
            SystemTray tray = SystemTray.getSystemTray();

            // Initialize a single tray icon once (removing it immediately can cancel the notification on some OSes)
            if (!trayInitAttempted) {
                trayInitAttempted = true;
                Image image = createTrayImage();
                trayIcon = new TrayIcon(image, "Masroofy");
                trayIcon.setImageAutoSize(true);
                trayIcon.setToolTip("Masroofy Budget Tracker");
                tray.add(trayIcon);
            }

            if (trayIcon == null) return false;

            TrayIcon.MessageType msgType =
                (level == Level.ERROR) ? TrayIcon.MessageType.ERROR : TrayIcon.MessageType.WARNING;

            trayIcon.displayMessage(currentTitle, message, msgType);
            System.out.println("[Notification] Sent: " + message);
            return true;

        } catch (AWTException | SecurityException e) {
            System.err.println("[Notification] SystemTray error: " + e.getMessage());
            return false;
        }
    }

    private Image createTrayImage() {
        int size = 16;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(201, 168, 76, 255)); // Masroofy gold
        g.fillRoundRect(0, 0, size, size, 4, 4);
        g.setColor(Color.BLACK);
        g.drawString("M", 4, 12);
        g.dispose();
        return img;
    }

    // maintenance() — clears old notifications from memory
    public void maintenance() {
        this.notifId      = 0;
        this.msg          = null;
        this.currentTitle = null;
        System.out.println("[Notification] Maintenance done.");
    }

    public String getMsg()          { return msg; }
    public String getCurrentTitle() { return currentTitle; }
    public int    getNotifId()      { return notifId; }
}
