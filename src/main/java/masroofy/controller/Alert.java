package masroofy.controller;

import java.time.LocalDate;

import masroofy.data.DAOLayer;
import masroofy.model.BudgetCycle;


public class Alert {

    private static final float THRESHOLD_WARNING   = 80f;
    private static final float THRESHOLD_EXHAUSTED = 100f;

    private final DAOLayer daoLayer;
    private final Notification notification;

    private float currentPercent;
    private String message;

    public Alert() {
        this.daoLayer = new DAOLayer();
        this.notification = new Notification();
    }

    // SD-6 Step 3: checkSpending()
    // Called after every expense is logged
    public void checkSpending(BudgetCycle cycle) {
        boolean percentSet = false;

        // Daily usage: percent of today's daily limit used
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
                "Daily limit reached! You have used 100% of your daily limit."
            );
        }

        // Total allowance usage: percent of total cycle budget used
        float totalAmount = cycle.getTotalAmount();
        if (totalAmount > 0) {
            float remaining = cycle.getRemainingBalance();
            float totalPercent = Math.max(0f, ((totalAmount - remaining) / totalAmount) * 100f);
            if (!percentSet) this.currentPercent = totalPercent;

            maybeFireAlert(
                cycle,
                totalPercent,
                "TOTAL",
                "Warning: You have used 80% of your total allowance.",
                "Budget Exhausted! You have used 100% of your total allowance."
            );
        }
    }

    private void maybeFireAlert(
        BudgetCycle cycle,
        float percent,
        String scopeKey,
        String warningMessage,
        String exhaustedMessage
    ) {
        String baseType = determineType(percent);
        if (baseType == null) return;

        String alertType = baseType + "_" + scopeKey;
        if (daoLayer.wasAlertFired(cycle.getBudgetCycleId(), alertType)) return;

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

    // SD-6 Step 4: determineType() : String
    // Returns alert type based on percent used
    private String determineType(float percent) {
        if (percent >= THRESHOLD_EXHAUSTED) return "EXHAUSTED";
        if (percent >= THRESHOLD_WARNING)   return "WARNING";
        return null;
    }

    public float  getCurrentPercent() { return currentPercent; }
    public String getMessage()        { return message; }
}
