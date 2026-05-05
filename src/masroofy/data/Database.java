package masroofy.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database
 *
 * MVC Role   : Data Layer
 * Class Diag : instance:DatabaseConn, connection:Connection
 *              getInstance(), getConnection(), closeConnection()
 * Pattern    : Singleton
 *
 * NOTE: Not Ramez's responsibility.
 *       Provided so controllers can compile.
 */
public class Database {

    private static Database  instance;
    private        Connection connection;

    private static final String DB_URL = "jdbc:sqlite:masroofy.db";

    // ── Private constructor (Singleton) ──────────────────────────────────────
    private Database() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(DB_URL);
            this.connection.setAutoCommit(true);
            System.out.println("[Database] Connected to SQLite: " + DB_URL);
            initTables();
        } catch (Exception e) {
            System.err.println("[Database] Connection failed: " + e.getMessage());
        }
    }

    // ── getInstance() — matches class diagram ────────────────────────────────
    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    // ── getConnection() — matches class diagram ──────────────────────────────
    public Connection getConnection() {
        return connection;
    }

    // ── closeConnection() — matches class diagram ────────────────────────────
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[Database] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[Database] Close error: " + e.getMessage());
        }
    }

    // ── Create tables if they don't exist ───────────────────────────────────
    private void initTables() {
        try (var stmt = connection.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Cycles (
                    budgetCycleId    INTEGER PRIMARY KEY AUTOINCREMENT,
                    totalAmount      REAL    NOT NULL,
                    startDate        TEXT    NOT NULL,
                    endDate          TEXT    NOT NULL,
                    remainingBalance REAL    NOT NULL,
                    safeDailyLimit   REAL    NOT NULL,
                    isActive         INTEGER DEFAULT 1
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Transactions (
                    expenseId      INTEGER PRIMARY KEY AUTOINCREMENT,
                    budgetCycleId  INTEGER NOT NULL,
                    amount         REAL    NOT NULL,
                    category       TEXT    NOT NULL,
                    date           TEXT    NOT NULL,
                    note           TEXT,
                    FOREIGN KEY (budgetCycleId) REFERENCES Cycles(budgetCycleId)
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Categories (
                    categoryId  INTEGER PRIMARY KEY AUTOINCREMENT,
                    name        TEXT    NOT NULL,
                    amount      REAL    DEFAULT 0,
                    isDefault   INTEGER DEFAULT 0
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Users (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    name        TEXT,
                    pinHash     TEXT,
                    isLocked    INTEGER DEFAULT 0
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS AlertLog (
                    alertId     INTEGER PRIMARY KEY AUTOINCREMENT,
                    cycleId     INTEGER NOT NULL,
                    alertType   TEXT    NOT NULL,
                    firedAt     TEXT    NOT NULL
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS DailySnapshots (
                    snapshotId   INTEGER PRIMARY KEY AUTOINCREMENT,
                    cycleId      INTEGER NOT NULL,
                    date         TEXT    NOT NULL,
                    dailyLimit   REAL    NOT NULL
                )""");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Settings (
                    key   TEXT PRIMARY KEY,
                    value TEXT
                )""");

            System.out.println("[Database] Tables initialized.");

        } catch (SQLException e) {
            System.err.println("[Database] initTables error: " + e.getMessage());
        }
    }
}
