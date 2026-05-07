package masroofy.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BudgetCycle {

    private int       budgetCycleId;
    private float     totalAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private float     remainingBalance;
    private float     safeDailyLimit;
    private float     totalBase;
    private int       remainingDays;
    private boolean   isActive;

    public BudgetCycle(float totalAmount, LocalDate startDate, LocalDate endDate) {
        this.totalAmount      = totalAmount;
        this.startDate        = startDate;
        this.endDate          = endDate;
        this.remainingBalance = totalAmount;
        this.isActive         = true;
    }

    public float calculateWeeklyLimit() {
        long totalDays     = ChronoUnit.DAYS.between(startDate, endDate);
        this.totalBase     = totalAmount;
        this.remainingDays = (int) totalDays;

        if (totalDays > 0) {
            this.safeDailyLimit = totalAmount / totalDays;
        } else {
            this.safeDailyLimit = totalAmount;
        }

        return this.safeDailyLimit;
    }

    // Backwards-compatible name used by older controller code.
    public float calculateBalance() {
        return calculateWeeklyLimit();
    }

    // ── updateBalance() — called after each expense (FR-4) ───────────────────
    public void updateBalance(float expenseAmount) {
        this.remainingBalance -= expenseAmount;
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        this.remainingDays = (int) daysLeft;
        if (daysLeft > 0) {
            this.safeDailyLimit = remainingBalance / daysLeft;
        }
    }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public int       getBudgetCycleId()           { return budgetCycleId; }
    public void      setBudgetCycleId(int id)     { this.budgetCycleId = id; }
    public float     getTotalAmount()             { return totalAmount; }
    public LocalDate getStartDate()               { return startDate; }
    public LocalDate getEndDate()                 { return endDate; }
    public float     getRemainingBalance()        { return remainingBalance; }
    public void      setRemainingBalance(float b) { this.remainingBalance = b; }
    public float     getSafeDailyLimit()          { return safeDailyLimit; }
    public void      setSafeDailyLimit(float l)   { this.safeDailyLimit = l; }
    public int       getRemainingDays()           { return remainingDays; }
    public boolean   isActive()                   { return isActive; }
    public void      setActive(boolean active)    { this.isActive = active; }
    public float     getTotalBase()               { return totalBase; }
}
