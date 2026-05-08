package masroofy.controller;

import java.time.LocalDate;

import masroofy.data.DAOLayer;
import masroofy.model.BudgetCycle;

/**
 * Controller responsible for creating and managing budget cycles.
 */
public class CycleController {

    private final DAOLayer daoLayer;
    private String validationError;

    /**
     * Creates a controller backed by the DAO layer.
     */
    public CycleController() {
        this.daoLayer = new DAOLayer();
    }

    /**
     * Creates and persists a new budget cycle after validating input.
     *
     * @param amount    total cycle budget amount
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive)
     * @return created cycle, or {@code null} when validation fails or persistence
     *         fails
     */
    public BudgetCycle createCycle(float amount, LocalDate startDate, LocalDate endDate) {
        if (!validateInput(amount, startDate, endDate))
            return null;

        BudgetCycle cycle = new BudgetCycle(amount, startDate, endDate);
        cycle.calculateWeeklyLimit();

        int cycleId = daoLayer.insertCycle(cycle);
        if (cycleId <= 0) {
            validationError = "Could not save budget cycle.";
            return null;
        }

        generateReport(cycle);
        return cycle;
    }

    /**
     * Returns the active cycle if one exists.
     *
     * @return active cycle, or {@code null}
     */
    public BudgetCycle checkActiveCycle() {
        return daoLayer.findActiveCycle();
    }

    /**
     * Resets the specified cycle and deletes related data.
     *
     * @param cycleId cycle id
     * @return {@code true} if reset succeeds
     */
    public boolean resetCycle(int cycleId) {
        return daoLayer.resetCycle(cycleId);
    }

    /**
     * Returns the last validation error message, if any.
     *
     * @return validation error (may be {@code null})
     */
    public String getValidationError() {
        return validationError;
    }

    /**
     * Emits a creation report message for debugging/logging.
     *
     * @param cycle created cycle
     */
    private void generateReport(BudgetCycle cycle) {
        System.out.println("[CycleController] Cycle created. ID=" + cycle.getBudgetCycleId()
                + " DailyLimit=" + cycle.getSafeDailyLimit());
    }

    /**
     * Validates cycle creation inputs and sets {@link #validationError} on failure.
     *
     * @param amount total budget amount
     * @param start  start date
     * @param end    end date
     * @return {@code true} if inputs are valid
     */
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
