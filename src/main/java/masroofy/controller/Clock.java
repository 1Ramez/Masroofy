package masroofy.controller;

import masroofy.data.DAOLayer;
import masroofy.model.BudgetCycle;

import java.time.LocalDate;

/**
 * Clock
 * MVC Role  : Controller
 * Class Diag: date, previousDayLimit, detect:float
 *             performCheck(), calculateRollover(), notifyStudent()
 *
 * SD-5 (US5): Daily Rollover Management
 *   [app opens — new day detected]
 *     → BudgetCycle.performCheck()
 *       → DAOLayer SELECT prevSpent, prevLimit    (Step 3)
 *       → calculateDeficit() : float              (Step 5)
 *       → [isNegative=true overspent]
 *           → BudgetCycle.UpdateBalance() reduce  (Step 6)
 *           → DAOLayer INSERT INTO DailySnapshots  (Step 7)
 *           → notifyDashboard() color ORANGE       (Step 8)
 *       → [isNegative=false saved]
 *           → BudgetCycle.UpdateBalance() increase (Step 9)
 *           → DAOLayer INSERT INTO DailySnapshots  (Step 10)
 *           → notifyDashboard() color GREEN        (Step 11)
 */
public class Clock {

    private final DAOLayer daoLayer;
    private String lastCheckDate;   // tracks last day checked to avoid double-run

    public Clock() {
        this.daoLayer = new DAOLayer();
    }

    // SD-5 Step 2: performCheck() : void
    // Called when app opens and a new day is detected
    public RolloverResult performCheck() {

        // SD-5 Step 1: get active cycle
        BudgetCycle cycle = daoLayer.findActiveCycle();
        if (cycle == null) return null;

        // SD-5 Step 3: SELECT prevSpent, prevLimit FROM DailySnapshots
        DAOLayer.Snapshot snapshot = daoLayer.getLatestSnapshot(cycle.getBudgetCycleId());
        // Avoid applying rollover more than once per calendar day.
        if (snapshot != null && LocalDate.now().equals(snapshot.checkDate())) {
            return null;
        }

        // Determine yesterday's spend from actual transactions.
        LocalDate yesterday = LocalDate.now().minusDays(1);
        float prevSpent = daoLayer.getTotalSpentOnDate(cycle.getBudgetCycleId(), yesterday);

        // SD-5 Step 5: calculateDeficit() : float
        float deficit = calculateDeficit(cycle, prevSpent);
        boolean isNegative = deficit < 0;

        // Dynamic rollover is handled automatically by:
        //   dailyLimitToday = (remainingBalance + spentToday) / remainingDays
        // so we don't mutate remainingBalance here.
        daoLayer.insertDailySnapshot(cycle.getBudgetCycleId(), prevSpent, deficit, isNegative);

        notifyDashboard(isNegative ? "ORANGE" : "GREEN", cycle.getSafeDailyLimit());

        return new RolloverResult(cycle, isNegative, deficit);
    }

    // SD-5 Step 5: calculateDeficit() : float
    // deficit = safeDailyLimit - prevSpent
    // negative = overspent, positive = saved
    private float calculateDeficit(BudgetCycle cycle, float prevSpent) {
        return cycle.getSafeDailyLimit() - prevSpent;
    }

    // SD-5 Step 8/11: notifyDashboard() — signals UI with color and new limit
    private void notifyDashboard(String color, float newLimit) {
        System.out.println("[Clock] Dashboard notified. Color=" + color
            + " NewLimit=" + newLimit);
    }

    /**
     * RolloverResult
     * Carries rollover outcome back to the UI (Dashboard)
     * so it can update the display color and limit value.
     */
    public static class RolloverResult {
        public final BudgetCycle cycle;
        public final boolean     isNegative;
        public final float       deficit;
        public final String      dashboardColor;

        public RolloverResult(BudgetCycle cycle, boolean isNegative, float deficit) {
            this.cycle          = cycle;
            this.isNegative     = isNegative;
            this.deficit        = deficit;
            this.dashboardColor = isNegative ? "ORANGE" : "GREEN";
        }
    }
}
