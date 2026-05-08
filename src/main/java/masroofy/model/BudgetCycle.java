package masroofy.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a budget cycle (allowance period) with derived daily limit values.
 *
 * <p>
 * The cycle is persisted in the {@code Cycles} table and is used by controllers
 * to compute and
 * display daily spending limits.
 * </p>
 */
public class BudgetCycle {

    private int budgetCycleId;
    private float totalAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private float remainingBalance;
    private float safeDailyLimit;
    private float totalBase;
    private int remainingDays;
    private float remainingDailyLimit;
    private boolean isActive;

    /**
     * Creates a new in-memory cycle with the provided budget and dates.
     *
     * @param totalAmount the total budget for the cycle
     * @param startDate   the start date (inclusive)
     * @param endDate     the end date (inclusive)
     */
    public BudgetCycle(float totalAmount, LocalDate startDate, LocalDate endDate) {
        this.totalAmount = totalAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.remainingBalance = totalAmount;
        this.totalBase = totalAmount;
        this.isActive = true;
    }

    /**
     * Calculates the initial safe daily limit based on the full cycle length.
     *
     * @return the computed safe daily limit
     */
    public float calculateWeeklyLimit() {
        long totalDaysInclusive = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        this.remainingDays = (int) Math.max(totalDaysInclusive, 1);
        if (totalDaysInclusive > 0) {
            this.safeDailyLimit = totalAmount / totalDaysInclusive;
        } else {
            this.safeDailyLimit = totalAmount;
        }
        return this.safeDailyLimit;
    }

    /**
     * Recomputes the number of remaining days based on today's date.
     *
     * <p>
     * Daily limits are derived by controllers using persisted remaining balance and
     * today's
     * spending.
     * </p>
     */
    public void calculateBalance() {
        long daysLeftInclusive = ChronoUnit.DAYS.between(LocalDate.now(), endDate) + 1;
        this.remainingDays = (int) Math.max(daysLeftInclusive, 1);
        this.remainingDailyLimit = safeDailyLimit;
    }

    /**
     * Applies an expense amount to the remaining balance.
     *
     * @param expenseAmount the expense amount to subtract
     */
    public void updateBalance(float expenseAmount) {
        this.remainingBalance -= expenseAmount;
    }

    /**
     * Returns the database id for this cycle.
     *
     * @return cycle id
     */
    public int getBudgetCycleId() {
        return budgetCycleId;
    }

    /**
     * Sets the database id for this cycle.
     *
     * @param id cycle id
     */
    public void setBudgetCycleId(int id) {
        this.budgetCycleId = id;
    }

    /**
     * Returns the total budget amount for the cycle.
     *
     * @return total budget amount
     */
    public float getTotalAmount() {
        return totalAmount;
    }

    /**
     * Returns the start date for the cycle.
     *
     * @return start date
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Returns the end date for the cycle.
     *
     * @return end date
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Returns the remaining balance for the cycle.
     *
     * @return remaining balance
     */
    public float getRemainingBalance() {
        return remainingBalance;
    }

    /**
     * Sets the remaining balance.
     *
     * @param b remaining balance
     */
    public void setRemainingBalance(float b) {
        this.remainingBalance = b;
    }

    /**
     * Returns the safe daily limit currently associated with this cycle.
     *
     * @return safe daily limit
     */
    public float getSafeDailyLimit() {
        return safeDailyLimit;
    }

    /**
     * Sets the safe daily limit value.
     *
     * @param l safe daily limit
     */
    public void setSafeDailyLimit(float l) {
        this.safeDailyLimit = l;
    }

    /**
     * Returns the number of remaining days in the cycle (inclusive).
     *
     * @return remaining days
     */
    public int getRemainingDays() {
        return remainingDays;
    }

    /**
     * Returns the remaining daily limit for today.
     *
     * @return remaining daily limit
     */
    public float getRemainingDailyLimit() {
        return remainingDailyLimit;
    }

    /**
     * Sets the remaining daily limit for today.
     *
     * @param remainingDailyLimit remaining daily limit
     */
    public void setRemainingDailyLimit(float remainingDailyLimit) {
        this.remainingDailyLimit = remainingDailyLimit;
    }

    /**
     * Returns whether this cycle is currently active.
     *
     * @return {@code true} if active
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets the active flag for the cycle.
     *
     * @param a active flag
     */
    public void setActive(boolean a) {
        this.isActive = a;
    }

    /**
     * Returns the base total budget amount used for calculations.
     *
     * @return total base amount
     */
    public float getTotalBase() {
        return totalBase;
    }
}
