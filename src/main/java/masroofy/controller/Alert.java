package masroofy.controller;

import java.time.LocalDate;

import masroofy.data.DAOLayer;
import masroofy.model.BudgetCycle;

/**
 * Evaluates spending thresholds and triggers user notifications.
 *
 * <p>
 * Alerts are de-duplicated by persisting an alert log entry per cycle and alert
 * type.
 * </p>
 */
public class Alert {

    private static final float THRESHOLD_WARNING = 80f;
    private static final float THRESHOLD_EXHAUSTED = 100f;

    private final DAOLayer daoLayer;
    private final Notification notification;

    private float currentPercent;
    private String message;

    /**
     * Creates an alert service backed by the DAO layer and notification system.
     */
    public Alert() {
        this.daoLayer = new DAOLayer();
        this.notification = new Notification();
    }

    /**
     * Checks spending for the given cycle and fires warnings/exhaustion alerts when
     * thresholds are
     * crossed.
     *
     * <p>
     * The check evaluates:
     * </p>
     * <ul>
     * <li>Daily usage: percentage of today's safe daily limit used</li>
     * <li>Total usage: percentage of the total cycle budget used</li>
     * </ul>
     *
     * @param cycle active cycle to evaluate
     */
    public void checkSpending(BudgetCycle cycle) {
        boolean percentSet = false;

        float dailyLimit = cycle.getSafeDailyLimit();
        if (dailyLimit > 0) {
            float remainingDaily = cycle.getRemainingDailyLimit();
            float usedToday = dailyLimit - remainingDaily;
            float dailyPercent = Math.max(0f, (usedToday / dailyLimit) * 100f);
            this.currentPercent = dailyPercent;
            percentSet = true;

            maybeFireAlert(
                    cycle,
                    dailyPercent,
                    "DAILY_" + LocalDate.now(),
                    "Warning: You have used 80% of your daily limit.",
                    "Daily limit reached! You have used 100% of your daily limit.");
        }

        float totalAmount = cycle.getTotalAmount();
        if (totalAmount > 0) {
            float remaining = cycle.getRemainingBalance();
            float totalPercent = Math.max(0f, ((totalAmount - remaining) / totalAmount) * 100f);
            if (!percentSet)
                this.currentPercent = totalPercent;

            maybeFireAlert(
                    cycle,
                    totalPercent,
                    "TOTAL",
                    "Warning: You have used 80% of your total allowance.",
                    "Budget Exhausted! You have used 100% of your total allowance.");
        }
    }

    /**
     * Fires an alert for a given scope if thresholds are reached and the alert
     * wasn't already fired.
     *
     * @param cycle            active cycle
     * @param percent          used percentage for the scope
     * @param scopeKey         scope key suffix used for de-duplication
     * @param warningMessage   message used when warning threshold is reached
     * @param exhaustedMessage message used when exhaustion threshold is reached
     */
    private void maybeFireAlert(
            BudgetCycle cycle,
            float percent,
            String scopeKey,
            String warningMessage,
            String exhaustedMessage) {
        String baseType = determineType(percent);
        if (baseType == null)
            return;

        String alertType = baseType + "_" + scopeKey;
        if (daoLayer.wasAlertFired(cycle.getBudgetCycleId(), alertType))
            return;

        if (baseType.equals("WARNING")) {
            message = warningMessage;
            daoLayer.insertAlertLog(cycle.getBudgetCycleId(), message, alertType);
            notification.send(message, Notification.Level.WARNING);
        } else if (baseType.equals("EXHAUSTED")) {
            message = exhaustedMessage;
            daoLayer.insertAlertLog(cycle.getBudgetCycleId(), message, alertType);
            notification.send(message, Notification.Level.ERROR);
        }
    }

    /**
     * Determines the alert type for a percent value.
     *
     * @param percent percentage used
     * @return {@code "EXHAUSTED"}, {@code "WARNING"}, or {@code null} when under
     *         thresholds
     */
    private String determineType(float percent) {
        if (percent >= THRESHOLD_EXHAUSTED)
            return "EXHAUSTED";
        if (percent >= THRESHOLD_WARNING)
            return "WARNING";
        return null;
    }

    /**
     * Returns the last percent value computed by
     * {@link #checkSpending(BudgetCycle)}.
     *
     * @return current percent
     */
    public float getCurrentPercent() {
        return currentPercent;
    }

    /**
     * Returns the last alert message produced by this instance.
     *
     * @return message (may be {@code null})
     */
    public String getMessage() {
        return message;
    }
}
