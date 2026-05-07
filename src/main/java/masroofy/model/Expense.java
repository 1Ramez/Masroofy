package masroofy.model;

import java.time.LocalDate;

/**
 * Expense
 * MVC Role  : Model (Entity)
 * Class Diag: expenseId, amount, date, category, budgetCycleId
 * SD-2      : logged via ExpenseController
 * SD-7      : persisted immediately via DAOLayer
 */
public class Expense {

    private int       expenseId;
    private float     amount;
    private int       categoryId;
    private int       budgetCycleId;
    private String    note;
    private LocalDate date;

    public Expense(float amount, int categoryId, int budgetCycleId, String note) {
        this.amount        = amount;
        this.categoryId    = categoryId;
        this.budgetCycleId = budgetCycleId;
        this.note          = note;
        this.date          = LocalDate.now();
    }

    // Getters & Setters
    public int       getExpenseId()             { return expenseId; }
    public void      setExpenseId(int id)       { this.expenseId = id; }
    public float     getAmount()               { return amount; }
    public void      setAmount(float a)        { this.amount = a; }
    public int       getCategoryId()           { return categoryId; }
    public void      setCategoryId(int c)      { this.categoryId = c; }
    public int       getBudgetCycleId()        { return budgetCycleId; }
    public void      setBudgetCycleId(int id)  { this.budgetCycleId = id; }
    public String    getNote()                 { return note; }
    public void      setNote(String n)         { this.note = n; }
    public LocalDate getDate()                 { return date; }
    public void      setDate(LocalDate d)      { this.date = d; }
}
