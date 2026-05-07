package masroofy.controller;

import masroofy.data.DAOLayer;
import masroofy.model.BudgetCycle;
import masroofy.model.Category;
import masroofy.model.Expense;

import java.time.LocalDate;
import java.util.List;

/**
 * ExpenseController
 * MVC Role  : Controller
 *
 * SD-2 (US2): Rapid Expense Logging
 *   ExpensePage → addExpense(amount, category)
 *     → DAOLayer.getCategoryList()       [getName() : list]
 *     → BudgetCycle.UpdateBalance()
 *     → BudgetCycle.calculateWeeklyLimit()
 *     → DAOLayer.insertExpense()         [INSERT INTO Transactions]
 *     → return expenseId
 *     → "Saved" redirect to Dashboard
 *   [invalid input] → "Please enter a valid number"
 *
 * SD-3 (US3): Dynamic Daily Limit View
 *   Dashboard → calculateBalance()
 *     → DAOLayer.findActiveCycle()       [SELECT remaining from cycles]
 *     → BudgetCycle.calculateWeeklyLimit()
 *     → DAOLayer.updateCycleSafeDailyLimit() [UPDATE cycles SET safeDailyLmt]
 *     → generateReport()
 */
public class ExpenseController {

    private final DAOLayer daoLayer;
    private String validationError;

    public ExpenseController() {
        this.daoLayer = new DAOLayer();
    }

    // ── SD-2: addExpense(amount, category) ───────────────────────────────────
    public Expense addExpense(float amount, int categoryId, String note) {

        // SD-2 alt [invalid input non numeric] — amount must be > 0
        if (amount <= 0) {
            validationError = "Please enter a valid number";
            return null;
        }

        // SD-2 Step 1: get active cycle
        BudgetCycle cycle = daoLayer.findActiveCycle();
        if (cycle == null) {
            validationError = "No active budget cycle found.";
            return null;
        }

        // SD-2 Step 4: UpdateBalance() : void — subtract expense from cycle
        cycle.updateBalance(amount);

        // SD-2 Step 6: INSERT INTO Transactions — returns expenseId (Step 7)
        Expense expense = new Expense(amount, categoryId, cycle.getBudgetCycleId(), note);
        int expenseId = daoLayer.insertExpense(expense);

        if (expenseId <= 0) {
            validationError = "Could not save expense.";
            return null;
        }

        // Persist updated cycle balance to DB
        daoLayer.updateCycleRemainingBalance(cycle.getBudgetCycleId(), cycle.getRemainingBalance());

        // SD-2 Step 8: "Saved" — redirect to Dashboard (signal via return)
        return expense;
    }

    // ── SD-2 Step 2: getCategories() — getName() : list ─────────────────────
    public List<Category> getCategories() {
        return daoLayer.getCategoryList();
    }

    // ── SD-3: refreshDashboard() — calculateBalance() : void ─────────────────
    // Called when Dashboard opens to get updated safe daily limit
    public BudgetCycle refreshDashboard() {

        // SD-3 Step 3: SELECT remaining from cycles
        BudgetCycle cycle = daoLayer.findActiveCycle();
        if (cycle == null) return null;

        // SD-3 Step 7: calculateBalance() : void
        cycle.calculateBalance();

        float spentToday = daoLayer.getTotalSpentOnDate(cycle.getBudgetCycleId(), LocalDate.now());
        float startOfDayRemaining = cycle.getRemainingBalance() + spentToday;
        float dailyLimitToday = startOfDayRemaining / cycle.getRemainingDays();
        cycle.setSafeDailyLimit(dailyLimitToday);
        cycle.setRemainingDailyLimit(dailyLimitToday - spentToday);

        // SD-3 Step 9: generateReport() : void
        generateReport(cycle);

        return cycle;
    }

    // ── SD-3 Step 9: generateReport() ───────────────────────────────────────
    private void generateReport(BudgetCycle cycle) {
        System.out.println("[ExpenseController] Dashboard refreshed."
            + " Remaining=" + cycle.getRemainingBalance()
            + " DailyLimit=" + cycle.getSafeDailyLimit());
    }

    public String getValidationError() { return validationError; }
}
