package masroofy.controller;

import masroofy.data.DAOLayer;
import masroofy.model.Student;
import masroofy.session.UserSession;
import masroofy.util.PinHasher;

/**
 * Handles registration, login, and logout.
 */
public class AuthController {

    private final DAOLayer daoLayer;
    private String validationError;

    public AuthController() {
        this.daoLayer = new DAOLayer();
    }

    public String getValidationError() {
        return validationError;
    }

    public Student restoreSelectedUser() {
        Student selected = daoLayer.getSelectedUser();
        if (selected != null) {
            UserSession.setCurrentUser(selected);
        }
        return selected;
    }

    public Student register(String name, String pin, String confirmPin) {
        validationError = null;
        String cleanName = name == null ? "" : name.trim();
        String cleanPin = pin == null ? "" : pin.trim();
        String cleanConfirm = confirmPin == null ? "" : confirmPin.trim();

        if (cleanName.isBlank()) {
            validationError = "Please enter a name.";
            return null;
        }
        if (cleanName.length() < 3) {
            validationError = "Name must be at least 3 characters.";
            return null;
        }
        if (cleanPin.length() < 4) {
            validationError = "PIN must be at least 4 digits.";
            return null;
        }
        if (!cleanPin.equals(cleanConfirm)) {
            validationError = "PIN confirmation does not match.";
            return null;
        }

        int id = daoLayer.insertUser(cleanName, PinHasher.sha256Hex(cleanPin));
        if (id <= 0) {
            validationError = "That name is already registered.";
            return null;
        }

        Student user = new Student(cleanName, "");
        user.setId(id);
        user.setSelected(true);
        daoLayer.selectUser(id);
        daoLayer.claimLegacyCyclesForUser(id);
        UserSession.setCurrentUser(user);
        return user;
    }

    public Student login(String name, String pin) {
        validationError = null;
        String cleanName = name == null ? "" : name.trim();
        String cleanPin = pin == null ? "" : pin.trim();

        if (cleanName.isBlank() || cleanPin.isBlank()) {
            validationError = "Please enter name and PIN.";
            return null;
        }

        Student user = daoLayer.findUserByName(cleanName);
        if (user == null) {
            validationError = "No account found with that name.";
            return null;
        }
        if (!PinHasher.matches(cleanPin, user.getPin())) {
            validationError = "Incorrect PIN.";
            return null;
        }

        daoLayer.selectUser(user.getId());
        daoLayer.claimLegacyCyclesForUser(user.getId());
        user.setSelected(true);
        UserSession.setCurrentUser(user);
        return user;
    }

    public void signOut() {
        daoLayer.clearSelectedUser();
        UserSession.clear();
    }
}
