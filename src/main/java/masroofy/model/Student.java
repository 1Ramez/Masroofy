package masroofy.model;

/**
 * Represents a user record.
 *
 * <p>Users are stored in the {@code Users} table.</p>
 */
public class Student {

    private int     id;
    private String  name;
    private String  pin;
    private boolean selected;

    /**
     * Creates a new student record.
     *
     * @param name user name
     * @param pin user pin
     */
    public Student(String name, String pin) {
        this.name = name;
        this.pin  = pin;
    }

    /**
     * Returns the database id.
     *
     * @return id
     */
    public int     getId()           { return id; }

    /**
     * Sets the database id.
     *
     * @param id id
     */
    public void    setId(int id)     { this.id = id; }

    /**
     * Returns the user name.
     *
     * @return name
     */
    public String  getName()         { return name; }

    /**
     * Returns the user pin.
     *
     * @return pin
     */
    public String  getPin()          { return pin; }

    /**
     * Sets the pin.
     *
     * @param p pin
     */
    public void    setPin(String p)  { this.pin = p; }

    /**
     * Returns whether this user is selected as the current user.
     *
     * @return {@code true} if selected
     */
    public boolean isSelected()      { return selected; }

    /**
     * Sets whether this user is selected as the current user.
     *
     * @param s selected flag
     */
    public void    setSelected(boolean s) { this.selected = s; }
}
