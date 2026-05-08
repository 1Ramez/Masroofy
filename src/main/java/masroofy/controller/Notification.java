package masroofy.controller;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;

import javafx.application.Platform;

/**
 * Sends user notifications for alerts.
 *
 * <p>
 * The primary delivery mechanism is the desktop OS notification area using AWT
 * {@link SystemTray}. When unavailable, the controller falls back to an in-app
 * JavaFX alert.
 * </p>
 */
public class Notification {

    private int notifId;
    private String currentTitle;
    private String type;
    private String msg;

    /**
     * Notification level used to map UI severity to platform notification types.
     */
    public enum Level {
        WARNING, ERROR
    }

    private static TrayIcon trayIcon;
    private static boolean trayInitAttempted = false;

    /**
     * Creates a new notification controller.
     */
    public Notification() {
        this.notifId = 0;
    }

    /**
     * Sends a warning notification.
     *
     * @param message message body
     */
    public void send(String message) {
        send(message, Level.WARNING);
    }

    /**
     * Sends a notification with the specified level.
     *
     * @param message message body
     * @param level   severity level
     */
    public void send(String message, Level level) {
        this.msg = message;
        this.currentTitle = "Masroofy Alert";
        this.notifId++;

        if (trySendSystemTray(message, level))
            return;

        try {
            Platform.runLater(() -> {
                javafx.scene.control.Alert.AlertType alertType = (level == Level.ERROR)
                        ? javafx.scene.control.Alert.AlertType.ERROR
                        : javafx.scene.control.Alert.AlertType.WARNING;

                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(alertType);
                alert.setTitle(currentTitle);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.show();
            });
        } catch (IllegalStateException ex) {
            System.out.println("[Notification] Message: " + message);
        }
    }

    /**
     * Attempts to send a desktop notification via the system tray.
     *
     * @param message message body
     * @param level   severity level
     * @return {@code true} if a system tray notification was sent
     */
    private boolean trySendSystemTray(String message, Level level) {
        if (!SystemTray.isSupported())
            return false;

        try {
            SystemTray tray = SystemTray.getSystemTray();

            if (!trayInitAttempted) {
                trayInitAttempted = true;
                Image image = createTrayImage();
                trayIcon = new TrayIcon(image, "Masroofy");
                trayIcon.setImageAutoSize(true);
                trayIcon.setToolTip("Masroofy Budget Tracker");
                tray.add(trayIcon);
            }

            if (trayIcon == null)
                return false;

            TrayIcon.MessageType msgType = (level == Level.ERROR) ? TrayIcon.MessageType.ERROR
                    : TrayIcon.MessageType.WARNING;

            trayIcon.displayMessage(currentTitle, message, msgType);
            System.out.println("[Notification] Sent: " + message);
            return true;

        } catch (AWTException | SecurityException e) {
            System.err.println("[Notification] SystemTray error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Creates a simple 16x16 tray icon image.
     *
     * @return tray icon image
     */
    private Image createTrayImage() {
        int size = 16;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(201, 168, 76, 255));
        g.fillRoundRect(0, 0, size, size, 4, 4);
        g.setColor(Color.BLACK);
        g.drawString("M", 4, 12);
        g.dispose();
        return img;
    }

    /**
     * Resets in-memory notification state.
     */
    public void maintenance() {
        this.notifId = 0;
        this.msg = null;
        this.currentTitle = null;
        System.out.println("[Notification] Maintenance done.");
    }

    /**
     * Returns the last message sent by this instance.
     *
     * @return message (may be {@code null})
     */
    public String getMsg() {
        return msg;
    }

    /**
     * Returns the current title used for notifications.
     *
     * @return title (may be {@code null})
     */
    public String getCurrentTitle() {
        return currentTitle;
    }

    /**
     * Returns the internal notification id counter.
     *
     * @return notification id
     */
    public int getNotifId() {
        return notifId;
    }
}
