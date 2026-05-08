package masroofy.model;

import java.time.LocalDate;

/**
 * Represents a single expense transaction within a budget cycle.
 *
 * <p>
 * Expenses are stored in the {@code Transactions} table.
 * </p>
 */
public class Expense {

    private int expenseId;
    private float amount;
    private int categoryId;
    private int budgetCycleId;
    private String note;
    private LocalDate date;

    /**
     * Creates a new expense instance with {@link LocalDate#now()} as its date.
     *
     * @param amount        expense amount
     * @param categoryId    referenced category id
     * @param budgetCycleId referenced cycle id
     * @param note          optional note
     */
    public Expense(float amount, int categoryId, int budgetCycleId, String note) {
        this.amount = amount;
        this.categoryId = categoryId;
        this.budgetCycleId = budgetCycleId;
        this.note = note;
        this.date = LocalDate.now();
    }

    /**
     * Returns the database id for this expense.
     *
     * @return expense id
     */
    public int getExpenseId() {
        return expenseId;
    }

    /**
     * Sets the database id for this expense.
     *
     * @param id expense id
     */
    public void setExpenseId(int id) {
        this.expenseId = id;
    }

    /**
     * Returns the expense amount.
     *
     * @return amount
     */
    public float getAmount() {
        return amount;
    }

    /**
     * Sets the expense amount.
     *
     * @param a amount
     */
    public void setAmount(float a) {
        this.amount = a;
    }

    /**
     * Returns the category id associated with this expense.
     *
     * @return category id
     */
    public int getCategoryId() {
        return categoryId;
    }

    /**
     * Sets the category id associated with this expense.
     *
     * @param c category id
     */
    public void setCategoryId(int c) {
        this.categoryId = c;
    }

    /**
     * Returns the budget cycle id associated with this expense.
     *
     * @return budget cycle id
     */
    public int getBudgetCycleId() {
        return budgetCycleId;
    }

    /**
     * Sets the budget cycle id associated with this expense.
     *
     * @param id budget cycle id
     */
    public void setBudgetCycleId(int id) {
        this.budgetCycleId = id;
    }

    /**
     * Returns the note associated with this expense.
     *
     * @return note (may be {@code null})
     */
    public String getNote() {
        return note;
    }

    /**
     * Sets the note associated with this expense.
     *
     * @param n note
     */
    public void setNote(String n) {
        this.note = n;
    }

    /**
     * Returns the expense date.
     *
     * @return date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Sets the expense date.
     *
     * @param d date
     */
    public void setDate(LocalDate d) {
        this.date = d;
    }
}
