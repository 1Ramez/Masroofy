package masroofy.controller;

import masroofy.data.DAOLayer;
import masroofy.model.BudgetCycle;

import java.time.LocalDate;

/**
 * Performs the daily rollover check for the active budget cycle.
 *
 * <p>If no snapshot exists for the current date, the clock calculates yesterday's spending and
 * persists a snapshot row that the UI can use to reflect the rollover outcome.</p>
 */
public class Clock {

    private final DAOLayer daoLayer;
    private String lastCheckDate;

    /**
     * Creates a clock backed by the DAO layer.
     */
    public Clock() {
        this.daoLayer = new DAOLayer();
    }

    /**
     * Runs the rollover check for the active cycle.
     *
     * @return a rollover result when a new snapshot is created, or {@code null} if no active cycle
     *         exists or rollover has already been applied for today
     */
    public RolloverResult performCheck() {
        BudgetCycle cycle = daoLayer.findActiveCycle();
        if (cycle == null) return null;

        DAOLayer.Snapshot snapshot = daoLayer.getLatestSnapshot(cycle.getBudgetCycleId());
        if (snapshot != null && LocalDate.now().equals(snapshot.checkDate())) {
            return null;
        }

        LocalDate yesterday = LocalDate.now().minusDays(1);
        float prevSpent = daoLayer.getTotalSpentOnDate(cycle.getBudgetCycleId(), yesterday);

        float deficit = calculateDeficit(cycle, prevSpent);
        boolean isNegative = deficit < 0;

        daoLayer.insertDailySnapshot(cycle.getBudgetCycleId(), prevSpent, deficit, isNegative);

        notifyDashboard(isNegative ? "ORANGE" : "GREEN", cycle.getSafeDailyLimit());

        return new RolloverResult(cycle, isNegative, deficit);
    }

    /**
     * Calculates the deficit for a day based on the cycle's safe daily limit.
     *
     * @param cycle active cycle
     * @param prevSpent total spent on the previous day
     * @return deficit value (negative when overspent)
     */
    private float calculateDeficit(BudgetCycle cycle, float prevSpent) {
        return cycle.getSafeDailyLimit() - prevSpent;
    }

    /**
     * Emits a dashboard notification signal for debugging/logging.
     *
     * @param color UI color to represent the rollover outcome
     * @param newLimit new daily limit value
     */
    private void notifyDashboard(String color, float newLimit) {
        System.out.println("[Clock] Dashboard notified. Color=" + color
            + " NewLimit=" + newLimit);
    }

    /**
     * Represents the outcome of a rollover check.
     */
    public static class RolloverResult {
        public final BudgetCycle cycle;
        public final boolean     isNegative;
        public final float       deficit;
        public final String      dashboardColor;

        /**
         * Creates a rollover result.
         *
         * @param cycle active cycle
         * @param isNegative whether the deficit is negative (overspent)
         * @param deficit deficit value
         */
        public RolloverResult(BudgetCycle cycle, boolean isNegative, float deficit) {
            this.cycle          = cycle;
            this.isNegative     = isNegative;
            this.deficit        = deficit;
            this.dashboardColor = isNegative ? "ORANGE" : "GREEN";
        }
    }
}

