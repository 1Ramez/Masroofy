package masroofy.model;

/**
 * Student
 * MVC Role  : Model (Entity)
 * Class Diag: id, name, pin, selected
 */
public class Student {

    private int     id;
    private String  name;
    private String  pin;
    private boolean selected;

    public Student(String name, String pin) {
        this.name = name;
        this.pin  = pin;
    }

    public int     getId()           { return id; }
    public void    setId(int id)     { this.id = id; }
    public String  getName()         { return name; }
    public String  getPin()          { return pin; }
    public void    setPin(String p)  { this.pin = p; }
    public boolean isSelected()      { return selected; }
    public void    setSelected(boolean s) { this.selected = s; }
}
