package masroofy.session;

import masroofy.model.Student;

/**
 * In-memory session holder for the currently logged-in user.
 */
public final class UserSession {

    private static Student currentUser;

    private UserSession() {
    }

    public static Student getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Student user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null && currentUser.getId() > 0;
    }

    public static int getCurrentUserId() {
        return isLoggedIn() ? currentUser.getId() : -1;
    }
}

