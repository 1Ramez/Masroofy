package masroofy.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the application's SQLite connection and ensures the schema exists.
 *
 * <p>
 * This is a lazy-initialized singleton used by {@link DAOLayer}.
 * </p>
 */
public class Database {

    private static Database instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:masroofy.db";

    /**
     * Creates the database connection and initializes required tables.
     */
    private Database() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            initTables();
            System.out.println("[Database] Connected.");
        } catch (Exception e) {
            System.err.println("[Database] Error: " + e.getMessage());
        }
    }

    /**
     * Returns the singleton database instance.
     *
     * @return the singleton instance
     */
    public static Database getInstance() {
        if (instance == null)
            instance = new Database();
        return instance;
    }

    /**
     * Returns the active JDBC connection.
     *
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Closes the underlying JDBC connection if it is open.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed())
                connection.close();
        } catch (SQLException e) {
            System.err.println("[Database] Close error: " + e.getMessage());
        }
    }

    /**
     * Creates the required tables and inserts default category rows when missing.
     *
     * @throws SQLException if schema initialization fails
     */
    private void initTables() throws SQLException {
        Statement s = connection.createStatement();

        s.execute("""
                CREATE TABLE IF NOT EXISTS Cycles (
                    budgetCycleId    INTEGER PRIMARY KEY AUTOINCREMENT,
                    userId           INTEGER,
                    totalAmount      REAL    NOT NULL,
                    startDate        TEXT    NOT NULL,
                    endDate          TEXT    NOT NULL,
                    remainingBalance REAL    NOT NULL,
                    safeDailyLimit   REAL    NOT NULL,
                    isActive         INTEGER DEFAULT 1
                )""");

        s.execute("""
                CREATE TABLE IF NOT EXISTS Transactions (
                    expenseId     INTEGER PRIMARY KEY AUTOINCREMENT,
                    budgetCycleId INTEGER NOT NULL,
                    amount        REAL    NOT NULL,
                    categoryId    INTEGER NOT NULL,
                    date          TEXT    NOT NULL,
                    note          TEXT,
                    FOREIGN KEY (budgetCycleId) REFERENCES Cycles(budgetCycleId)
                )""");

        s.execute("""
                CREATE TABLE IF NOT EXISTS Categories (
                    categoryId INTEGER PRIMARY KEY AUTOINCREMENT,
                    name       TEXT    NOT NULL,
                    amount     REAL    DEFAULT 0,
                    isDefault  INTEGER DEFAULT 0
                )""");

        s.execute("""
                CREATE TABLE IF NOT EXISTS Users (
                    id       INTEGER PRIMARY KEY AUTOINCREMENT,
                    name     TEXT,
                    pin      TEXT,
                    selected INTEGER DEFAULT 0
                )""");

        s.execute("""
                CREATE TABLE IF NOT EXISTS AlertLog (
                    alertId   INTEGER PRIMARY KEY AUTOINCREMENT,
                    cycleId   INTEGER NOT NULL,
                    message   TEXT    NOT NULL,
                    type      TEXT    NOT NULL,
                    isRead    INTEGER DEFAULT 0,
                    createdAt TEXT    NOT NULL
                )""");

        s.execute("""
                CREATE TABLE IF NOT EXISTS DailySnapshots (
                    snapshotId INTEGER PRIMARY KEY AUTOINCREMENT,
                    cycleId    INTEGER NOT NULL,
                    checkDate  TEXT    NOT NULL,
                    prevSpent  REAL    NOT NULL,
                    deficit    REAL    NOT NULL,
                    isNegative INTEGER NOT NULL
                )""");

        s.execute("""
                CREATE TABLE IF NOT EXISTS Settings (
                    key   TEXT PRIMARY KEY,
                    value TEXT
                )""");

        s.execute("""
                INSERT OR IGNORE INTO Categories (categoryId, name, isDefault)
                VALUES (1,'Food',1),(2,'Transport',1),(3,'Entertainment',1),(4,'Other',1)
                """);

        ensureColumnExists("Cycles", "userId", "INTEGER");

        s.execute("CREATE INDEX IF NOT EXISTS idx_cycles_user_active ON Cycles(userId, isActive)");
        s.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_users_name_unique ON Users(name)");

        migrateLegacyCycleOwnership();

        s.close();
        System.out.println("[Database] Tables ready.");
    }

    private void ensureColumnExists(String table, String column, String definition) throws SQLException {
        if (hasColumn(table, column))
            return;
        try (Statement s = connection.createStatement()) {
            s.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }

    private boolean hasColumn(String table, String column) throws SQLException {
        try (Statement s = connection.createStatement();
                ResultSet rs = s.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) {
                String name = rs.getString("name");
                if (column.equalsIgnoreCase(name))
                    return true;
            }
            return false;
        }
    }

    /**
     * Best-effort migration for older databases that predate multi-user support.
     *
     * <p>
     * If cycles exist with {@code userId} unset and there is exactly one user,
     * assigns all legacy cycles to that user to preserve existing progress.
     * </p>
     */
    private void migrateLegacyCycleOwnership() {
        try (Statement s = connection.createStatement()) {
            int cyclesWithUser = scalarInt(s, "SELECT COUNT(*) FROM Cycles WHERE userId IS NOT NULL");
            if (cyclesWithUser > 0)
                return;

            int userCount = scalarInt(s, "SELECT COUNT(*) FROM Users");
            if (userCount != 1)
                return;

            int userId = scalarInt(s, "SELECT id FROM Users WHERE selected=1 LIMIT 1");
            if (userId <= 0)
                userId = scalarInt(s, "SELECT id FROM Users LIMIT 1");
            if (userId <= 0)
                return;

            s.executeUpdate("UPDATE Cycles SET userId=" + userId + " WHERE userId IS NULL");
        } catch (SQLException e) {
            System.err.println("[Database] migrateLegacyCycleOwnership: " + e.getMessage());
        }
    }

    private int scalarInt(Statement s, String sql) throws SQLException {
        try (ResultSet rs = s.executeQuery(sql)) {
            if (rs.next())
                return rs.getInt(1);
            return 0;
        }
    }
}
