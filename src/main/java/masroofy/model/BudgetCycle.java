package masroofy.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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

    // Used in SD-1
    public BudgetCycle(float totalAmount, LocalDate startDate, LocalDate endDate){
        this.totalAmount = totalAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.remainingBalance = totalAmount;
        this.totalBase = totalAmount;
        this.isActive = true;
    }

    // Used in SD-1
    public float calculateWeeklyLimit(){
        long totalDaysInclusive = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        this.remainingDays = (int) Math.max(totalDaysInclusive, 1);
        if (totalDaysInclusive > 0) {
            this.safeDailyLimit = totalAmount / totalDaysInclusive;
        } else {
            this.safeDailyLimit = totalAmount;
        }
        return this.safeDailyLimit;
    }

    // SD-3 Step 2: calculateBalance() : void
    // Recalculates remainingDays for today (daily limits are derived in the controller from DB + today's spending)
    public void calculateBalance(){
        long daysLeftInclusive = ChronoUnit.DAYS.between(LocalDate.now(), endDate) + 1;
        this.remainingDays = (int) Math.max(daysLeftInclusive, 1);
        this.remainingDailyLimit = safeDailyLimit;
    }

    // SD-2 Step 4 / SD-5 Step 6+9: UpdateBalance() : void
    // Updates remaining balance after an expense is logged
    public void updateBalance(float expenseAmount){
        this.remainingBalance -= expenseAmount;
    }

    // Getters & Setters
    public int getBudgetCycleId(){
        return budgetCycleId;
    }

    public void setBudgetCycleId(int id){
        this.budgetCycleId = id;
    }

    public float getTotalAmount(){
        return totalAmount;
    }

    public LocalDate getStartDate(){
        return startDate;
    }

    public LocalDate getEndDate(){
        return endDate;
    }

    public float getRemainingBalance(){
        return remainingBalance;
    }

    public void setRemainingBalance(float b){
        this.remainingBalance = b;
    }

    public float getSafeDailyLimit(){
        return safeDailyLimit;
    }

    public void setSafeDailyLimit(float l){
        this.safeDailyLimit = l;
    }

    public int getRemainingDays(){
        return remainingDays;
    }

    public float getRemainingDailyLimit() { return remainingDailyLimit; }

    public void setRemainingDailyLimit(float remainingDailyLimit) {
        this.remainingDailyLimit = remainingDailyLimit;
    }

    public boolean isActive(){
        return isActive;
    }

    public void setActive(boolean a){
        this.isActive = a;
    }

    public float getTotalBase(){
        return totalBase;
    }
}
