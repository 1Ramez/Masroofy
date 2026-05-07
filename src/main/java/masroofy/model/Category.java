package masroofy.model;

/**
 * Represents an expense category.
 *
 * <p>Categories are stored in the {@code Categories} table and are referenced by expenses.</p>
 */
public class Category {

    private int     categoryId;
    private String  name;
    private float   amount;
    private boolean isDefault;

    /**
     * Creates a category value object.
     *
     * @param categoryId database id
     * @param name category display name
     * @param amount aggregated amount (when applicable)
     * @param isDefault whether this category is part of the default seed set
     */
    public Category(int categoryId, String name, float amount, boolean isDefault) {
        this.categoryId = categoryId;
        this.name       = name;
        this.amount     = amount;
        this.isDefault  = isDefault;
    }

    /**
     * Returns the category name.
     *
     * @return category name
     */
    public String getName()          { return name; }

    /**
     * Returns the category id.
     *
     * @return category id
     */
    public int    getCategoryId()    { return categoryId; }

    /**
     * Returns the amount associated with the category (when queried with aggregates).
     *
     * @return amount
     */
    public float  getAmount()        { return amount; }

    /**
     * Returns whether this is a default seeded category.
     *
     * @return {@code true} if default
     */
    public boolean isDefault()       { return isDefault; }

    /**
     * Updates the category name in-memory.
     *
     * @param newName new name
     */
    public void updateCategory(String newName) { this.name = newName; }

    /**
     * Returns the category name for UI display.
     *
     * @return category name
     */
    @Override
    public String toString() { return name; }
}
