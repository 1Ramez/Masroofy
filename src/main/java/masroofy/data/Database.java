package masroofy.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the application's SQLite connection and ensures the schema exists.
 *
 * <p>This is a lazy-initialized singleton used by {@link DAOLayer}.</p>
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
        if (instance == null) instance = new Database();
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

        s.close();
        System.out.println("[Database] Tables ready.");
    }
}
