package masroofy.model;

/**
 * Category
 * MVC Role  : Model (Entity)
 * Class Diag: categoryId, name, amount, isDefault
 * SD-2      : getCategoryList() returns List of Category
 */
public class Category {

    private int     categoryId;
    private String  name;
    private float   amount;
    private boolean isDefault;

    public Category(int categoryId, String name, float amount, boolean isDefault) {
        this.categoryId = categoryId;
        this.name       = name;
        this.amount     = amount;
        this.isDefault  = isDefault;
    }

    public String getName()          { return name; }
    public int    getCategoryId()    { return categoryId; }
    public float  getAmount()        { return amount; }
    public boolean isDefault()       { return isDefault; }

    public void updateCategory(String newName) { this.name = newName; }

    @Override
    public String toString() { return name; }
}
