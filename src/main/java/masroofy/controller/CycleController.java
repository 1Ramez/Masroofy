package masroofy.controller;

import java.time.LocalDate;

import masroofy.data.DAOLayer;
import masroofy.model.BudgetCycle;

public class CycleController {

    private final DAOLayer daoLayer;
    private String validationError;

    public CycleController(){
        this.daoLayer = new DAOLayer();
    }

    //Used in SD-1
    public BudgetCycle createCycle(float amount, LocalDate startDate, LocalDate endDate) {

        if (!validateInput(amount, startDate, endDate)) return null;

        BudgetCycle cycle = new BudgetCycle(amount, startDate, endDate);

        cycle.calculateWeeklyLimit();

        // SD-1 Step 4: INSERT INTO Cycles — returns cycleId (Step 5)
        int cycleId = daoLayer.insertCycle(cycle);
        if (cycleId <= 0) {
            validationError = "Could not save budget cycle.";
            return null;
        }

        // SD-1 Step 6: generateReport() : void
        generateReport(cycle);

        // SD-1 Step 7: signal UI to navigate to Dashboard
        return cycle;
    }

    // Called on app launch — if active cycle exists, skip InitSetupPage
    public BudgetCycle checkActiveCycle() {
        return daoLayer.findActiveCycle();
    }

    // Used in Settings: reset/delete the current cycle
    public boolean resetCycle(int cycleId) {
        return daoLayer.resetCycle(cycleId);
    }

    public String getValidationError() { return validationError; }

    // SD-1 Step 6: generateReport() — signals Dashboard to display data
    private void generateReport(BudgetCycle cycle) {
        System.out.println("[CycleController] Cycle created. ID=" + cycle.getBudgetCycleId()
            + " DailyLimit=" + cycle.getSafeDailyLimit());
    }

    // SD-1 alt: validation — amount > 0, dates not null, end > start
    private boolean validateInput(float amount, LocalDate start, LocalDate end) {
        validationError = null;
        if (amount <= 0) {
            validationError = "Allowance must be a positive number";
            return false;
        }
        if (start == null || end == null) {
            validationError = "Start date and end date are required";
            return false;
        }
        if (!end.isAfter(start)) {
            validationError = "End date must be after start date";
            return false;
        }
        return true;
    }
}
