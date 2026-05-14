package masroofy.controller;

import java.time.LocalDate;
import java.util.List;

import masroofy.data.DAOLayer;
import masroofy.model.BudgetCycle;
import masroofy.model.Category;
import masroofy.model.Expense;
import masroofy.session.UserSession;

/**
 * Controller for expense logging and dashboard refresh calculations.
 */
public class ExpenseController {

    private final DAOLayer daoLayer;
    private String validationError;

    /**
     * Creates a controller backed by the DAO layer.
     */
    public ExpenseController() {
        this.daoLayer = new DAOLayer();
    }

    /**
     * Creates and persists an expense for the active cycle.
     *
     * @param amount     expense amount (must be positive)
     * @param categoryId category id
     * @param note       optional note
     * @return created expense, or {@code null} on validation/persistence failure
     */
    public Expense addExpense(float amount, int categoryId, String note) {
        if (amount <= 0) {
            validationError = "Please enter a valid number";
            return null;
        }

        int userId = UserSession.getCurrentUserId();
        if (userId <= 0) {
            validationError = "Please log in first.";
            return null;
        }

        BudgetCycle cycle = daoLayer.findActiveCycleForUser(userId);
        if (cycle == null) {
            validationError = "No active budget cycle found.";
            return null;
        }

        cycle.updateBalance(amount);

        Expense expense = new Expense(amount, categoryId, cycle.getBudgetCycleId(), note);
        int expenseId = daoLayer.insertExpense(expense);

        if (expenseId <= 0) {
            validationError = "Could not save expense.";
            return null;
        }

        daoLayer.updateCycleRemainingBalance(cycle.getBudgetCycleId(), cycle.getRemainingBalance());

        return expense;
    }

    /**
     * Loads the category list for UI selection.
     *
     * @return list of categories
     */
    public List<Category> getCategories() {
        return daoLayer.getCategoryList();
    }

    /**
     * Reloads the active cycle and computes the safe daily limit for today based on
     * remaining
     * balance and spending so far.
     *
     * @return refreshed cycle, or {@code null} if no active cycle exists
     */
    public BudgetCycle refreshDashboard() {
        int userId = UserSession.getCurrentUserId();
        if (userId <= 0)
            return null;

        BudgetCycle cycle = daoLayer.findActiveCycleForUser(userId);
        if (cycle == null)
            return null;

        cycle.calculateBalance();

        float spentToday = daoLayer.getTotalSpentOnDate(cycle.getBudgetCycleId(), LocalDate.now());
        float startOfDayRemaining = cycle.getRemainingBalance() + spentToday;
        float dailyLimitToday = startOfDayRemaining / cycle.getRemainingDays();
        cycle.setSafeDailyLimit(dailyLimitToday);
        cycle.setRemainingDailyLimit(dailyLimitToday - spentToday);

        generateReport(cycle);

        return cycle;
    }

    /**
     * Emits a refresh report message for debugging/logging.
     *
     * @param cycle refreshed cycle
     */
    private void generateReport(BudgetCycle cycle) {
        System.out.println("[ExpenseController] Dashboard refreshed."
                + " Remaining=" + cycle.getRemainingBalance()
                + " DailyLimit=" + cycle.getSafeDailyLimit());
    }

    /**
     * Returns the last validation error message, if any.
     *
     * @return validation error (may be {@code null})
     */
    public String getValidationError() {
        return validationError;
    }
}
